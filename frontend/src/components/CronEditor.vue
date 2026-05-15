<template>
  <div class="cron-editor">
    <div class="cron-preview">
      <span class="preview-label">Cron表达式：</span>
      <span class="preview-value">{{ cronExpression }}</span>
    </div>
    
    <div class="cron-fields">
      <div class="field-item">
        <label>秒</label>
        <el-select v-model="fields.second" class="field-select" multiple collapse-tags>
          <el-option v-for="i in 60" :key="i - 1" :label="String(i - 1)" :value="String(i - 1)" />
          <el-option label="*" value="*" />
          <el-option label="每隔5秒" value="0/5" />
          <el-option label="每隔10秒" value="0/10" />
        </el-select>
      </div>
      
      <div class="field-item">
        <label>分</label>
        <el-select v-model="fields.minute" class="field-select" multiple collapse-tags>
          <el-option v-for="i in 60" :key="i - 1" :label="String(i - 1)" :value="String(i - 1)" />
          <el-option label="*" value="*" />
          <el-option label="每隔5分" value="0/5" />
          <el-option label="每隔15分" value="0/15" />
          <el-option label="每隔30分" value="0/30" />
        </el-select>
      </div>
      
      <div class="field-item">
        <label>时</label>
        <el-select v-model="fields.hour" class="field-select" multiple collapse-tags>
          <el-option v-for="i in 24" :key="i - 1" :label="String(i - 1)" :value="String(i - 1)" />
          <el-option label="*" value="*" />
          <el-option label="工作时间(9-18点)" value="9-18" />
        </el-select>
      </div>
      
      <div class="field-item">
        <label>日</label>
        <el-select v-model="fields.day" class="field-select" multiple collapse-tags>
          <el-option v-for="i in 31" :key="i" :label="String(i)" :value="String(i)" />
          <el-option label="*" value="*" />
          <el-option label="每月1号" value="1" />
          <el-option label="每月15号" value="15" />
          <el-option label="每月最后一天" value="L" />
        </el-select>
      </div>
      
      <div class="field-item">
        <label>月</label>
        <el-select v-model="fields.month" class="field-select" multiple collapse-tags>
          <el-option v-for="i in 12" :key="i" :label="String(i)" :value="String(i)" />
          <el-option label="*" value="*" />
          <el-option label="一季度" value="1-3" />
          <el-option label="二季度" value="4-6" />
          <el-option label="三季度" value="7-9" />
          <el-option label="四季度" value="10-12" />
        </el-select>
      </div>
      
      <div class="field-item">
        <label>周</label>
        <el-select v-model="fields.week" class="field-select" multiple collapse-tags>
          <el-option label="*" value="?" />
          <el-option label="周一" value="1" />
          <el-option label="周二" value="2" />
          <el-option label="周三" value="3" />
          <el-option label="周四" value="4" />
          <el-option label="周五" value="5" />
          <el-option label="周六" value="6" />
          <el-option label="周日" value="7" />
          <el-option label="工作日" value="1-5" />
          <el-option label="周末" value="6,7" />
        </el-select>
      </div>
    </div>
    
    <div class="cron-presets">
      <span class="presets-label">常用表达式：</span>
      <el-button v-for="preset in presets" :key="preset.value" size="small" @click="applyPreset(preset)">
        {{ preset.label }}
      </el-button>
    </div>
    
    <div class="cron-description">
      <span class="description-label">表达式说明：</span>
      <span class="description-text">{{ description }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface CronFields {
  second: string
  minute: string
  hour: string
  day: string
  month: string
  week: string
}

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const fields = ref<CronFields>({
  second: '0',
  minute: '0',
  hour: '*',
  day: '*',
  month: '*',
  week: '?'
})

const presets = [
  { label: '每分钟', value: '0 * * * * ?' },
  { label: '每小时', value: '0 0 * * * ?' },
  { label: '每天凌晨2点', value: '0 0 2 * * ?' },
  { label: '每天早上8点', value: '0 0 8 * * ?' },
  { label: '每天晚上10点', value: '0 0 22 * * ?' },
  { label: '每周一', value: '0 0 0 ? * 1' },
  { label: '每周五', value: '0 0 0 ? * 5' },
  { label: '每月1号', value: '0 0 0 1 * ?' },
  { label: '每月15号', value: '0 0 0 15 * ?' },
  { label: '每季度1号', value: '0 0 0 1 1,4,7,10 ?' },
  { label: '每年1月1号', value: '0 0 0 1 1 ?' }
]

const cronExpression = computed(() => {
  return `${fields.value.second} ${fields.value.minute} ${fields.value.hour} ${fields.value.day} ${fields.value.month} ${fields.value.week}`
})

const description = computed(() => {
  const { second, minute, hour, day, month, week } = fields.value
  const parts: string[] = []
  
  if (week !== '?') {
    const weekMap: Record<string, string> = {
      '1': '周一', '2': '周二', '3': '周三', '4': '周四', '5': '周五', '6': '周六', '7': '周日',
      '1-5': '工作日', '6,7': '周末'
    }
    parts.push(`每周${weekMap[week] || week}`)
  }
  
  if (day !== '*' && week === '?') {
    parts.push(`每月${day === 'L' ? '最后一天' : `第${day}天`}`)
  }
  
  if (month !== '*') {
    parts.push(`每年${month}`)
  }
  
  if (hour !== '*') {
    parts.push(`${hour}点`)
  }
  
  if (minute !== '*') {
    if (minute === '0') {
      parts.push('整点')
    } else {
      parts.push(`第${minute}分钟`)
    }
  }
  
  if (second !== '0') {
    parts.push(`第${second}秒`)
  }
  
  return parts.length > 0 ? parts.join(' ') : '每分钟执行'
})

const applyPreset = (preset: { label: string; value: string }) => {
  const parts = preset.value.split(' ')
  if (parts.length === 6) {
    fields.value = {
      second: parts[0],
      minute: parts[1],
      hour: parts[2],
      day: parts[3],
      month: parts[4],
      week: parts[5]
    }
  }
}

watch(cronExpression, (val) => {
  emit('update:modelValue', val)
})

watch(() => props.modelValue, (val) => {
  if (val) {
    const parts = val.split(' ')
    if (parts.length === 6) {
      fields.value = {
        second: parts[0],
        minute: parts[1],
        hour: parts[2],
        day: parts[3],
        month: parts[4],
        week: parts[5]
      }
    }
  }
}, { immediate: true })
</script>

<style scoped>
.cron-editor {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.cron-preview {
  margin-bottom: 16px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.preview-label {
  color: #666;
  margin-right: 8px;
}

.preview-value {
  font-family: monospace;
  color: #1890ff;
  font-weight: bold;
}

.cron-fields {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.field-item {
  display: flex;
  flex-direction: column;
}

.field-item label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.field-select {
  width: 100%;
}

.cron-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.presets-label {
  font-size: 12px;
  color: #666;
  margin-right: 8px;
  line-height: 32px;
}

.cron-description {
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
  border-left: 4px solid #1890ff;
}

.description-label {
  font-size: 12px;
  color: #666;
  margin-right: 8px;
}

.description-text {
  font-size: 14px;
  color: #333;
}

@media (max-width: 768px) {
  .cron-fields {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
