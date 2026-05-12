<template>
  <el-dialog
    :title="task ? '编辑任务' : '新建任务'"
    v-model="visible"
    width="700px"
    @close="handleClose"
  >
    <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
      <el-form-item label="任务名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入任务名称" />
      </el-form-item>
      <el-form-item label="任务类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择任务类型" class="w-full">
          <el-option label="Shell脚本" value="SHELL" />
          <el-option label="Python脚本" value="PYTHON" />
          <el-option label="HTTP请求" value="HTTP" />
          <el-option label="Java程序" value="JAVA" />
        </el-select>
      </el-form-item>
      <el-form-item label="执行命令" prop="command">
        <el-input
          v-model="form.command"
          type="textarea"
          :rows="3"
          placeholder="请输入执行命令，如: python script.py"
        />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="参数">
        <el-input v-model="form.parameters" placeholder="可选参数，如: --input=data.json" />
      </el-form-item>
      <el-form-item label="工作目录">
        <el-input v-model="form.workingDir" placeholder="可选，指定执行目录" />
      </el-form-item>
      <el-form-item label="环境变量">
        <el-input v-model="form.envVars" placeholder="可选，格式: KEY1=VALUE1,KEY2=VALUE2" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { taskApi, type Task } from '@/api/task'

const props = defineProps<{
  task?: Task | null
}>()

const emit = defineEmits<{
  close: []
  success: []
}>()

const visible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = ref<Task>({
  name: '',
  type: 'SHELL',
  command: '',
  description: '',
  parameters: '',
  envVars: '',
  workingDir: '',
  tags: ''
})

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  command: [{ required: true, message: '请输入执行命令', trigger: 'blur' }]
}

watch(() => props.task, (task) => {
  if (task) {
    form.value = { ...task }
    visible.value = true
  } else {
    form.value = {
      name: '',
      type: 'SHELL',
      command: '',
      description: '',
      parameters: '',
      envVars: '',
      workingDir: '',
      tags: ''
    }
    visible.value = true
  }
}, { immediate: true })

const handleClose = () => {
  visible.value = false
  emit('close')
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (props.task?.id) {
      await taskApi.update(props.task.id, form.value)
    } else {
      await taskApi.create(form.value)
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
