<template>
  <el-dialog
    :title="schedule ? '编辑调度' : '新建调度'"
    v-model="visible"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
      <el-form-item label="调度类型" prop="scheduleType">
        <el-radio-group v-model="form.scheduleType" @change="handleTypeChange">
          <el-radio label="CRON">Cron表达式</el-radio>
          <el-radio label="FIXED_INTERVAL">固定间隔</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="Cron表达式" prop="cronExpression" v-if="form.scheduleType === 'CRON'">
        <el-input v-model="form.cronExpression" placeholder="如: 0 0 * * * ?">
          <template #append>
            <el-tooltip content="秒 分 时 日 月 周 [年]" placement="top">
              <el-button>?</el-button>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>

      <template v-else>
        <el-form-item label="间隔" prop="fixedInterval">
          <el-input-number v-model="form.fixedInterval" :min="1" :max="86400" />
        </el-form-item>
        <el-form-item label="时间单位" prop="timeUnit">
          <el-select v-model="form.timeUnit" class="w-full">
            <el-option label="秒" value="SECONDS" />
            <el-option label="分钟" value="MINUTES" />
            <el-option label="小时" value="HOURS" />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="超时策略">
        <el-select v-model="form.misfirePolicy" class="w-full">
          <el-option label="智能策略" :value="0" />
          <el-option label="立即执行" :value="1" />
          <el-option label="放弃" :value="2" />
        </el-select>
      </el-form-item>

      <el-form-item label="时区">
        <el-input v-model="form.timezone" placeholder="默认: Asia/Shanghai" />
      </el-form-item>

      <el-form-item label="生效时间">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="w-full"
        />
      </el-form-item>

      <el-form-item label="最大并发数">
        <el-input-number v-model="form.maxConcurrent" :min="1" :max="10" />
      </el-form-item>

      <el-form-item label="启用">
        <el-switch v-model="formEnabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { scheduleApi, type TaskSchedule } from '@/api/task'

const props = defineProps<{
  schedule?: TaskSchedule | null
  taskId?: number
}>()

const emit = defineEmits<{
  close: []
  success: []
}>()

const visible = ref(false)
const submitting = ref(false)
const formRef = ref()
const dateRange = ref<[Date, Date] | null>(null)

const form = ref<TaskSchedule>({
  taskId: 0,
  scheduleType: 'CRON',
  cronExpression: '0 0 * * * ?',
  fixedInterval: 60,
  timeUnit: 'SECONDS',
  misfirePolicy: 0,
  timezone: 'Asia/Shanghai',
  maxConcurrent: 1,
  enabled: 1
})

const formEnabled = computed({
  get: () => form.value.enabled === 1,
  set: (val) => { form.value.enabled = val ? 1 : 0 }
})

const rules = {
  scheduleType: [{ required: true, message: '请选择调度类型', trigger: 'change' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }],
  fixedInterval: [{ required: true, message: '请输入间隔', trigger: 'blur' }],
  timeUnit: [{ required: true, message: '请选择时间单位', trigger: 'change' }]
}

watch(() => props.schedule, (schedule) => {
  if (schedule) {
    form.value = { ...schedule }
    if (schedule.startTime && schedule.endTime) {
      dateRange.value = [new Date(schedule.startTime), new Date(schedule.endTime)]
    }
    visible.value = true
  } else {
    form.value = {
      taskId: props.taskId || 0,
      scheduleType: 'CRON',
      cronExpression: '0 0 * * * ?',
      fixedInterval: 60,
      timeUnit: 'SECONDS',
      misfirePolicy: 0,
      timezone: 'Asia/Shanghai',
      maxConcurrent: 1,
      enabled: 1
    }
    dateRange.value = null
    visible.value = true
  }
}, { immediate: true })

const handleTypeChange = () => {
  if (form.value.scheduleType === 'CRON') {
    form.value.cronExpression = '0 0 * * * ?'
  } else {
    form.value.fixedInterval = 60
    form.value.timeUnit = 'SECONDS'
  }
}

const handleClose = () => {
  visible.value = false
  emit('close')
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (dateRange.value) {
      form.value.startTime = dateRange.value[0].toISOString()
      form.value.endTime = dateRange.value[1].toISOString()
    }

    if (props.schedule?.id) {
      await scheduleApi.update(props.schedule.id, form.value)
    } else {
      await scheduleApi.create(form.value)
    }
    ElMessage.success('保存成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitting.value = false
  }
}
</script>
