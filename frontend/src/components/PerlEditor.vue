<template>
  <div class="perl-editor">
    <div class="perl-editor-toolbar">
      <el-button size="small" @click="formatCode">格式化</el-button>
      <el-button size="small" @click="clearContent">清空</el-button>
    </div>
    <div class="perl-editor-content">
      <div class="line-numbers">
        <div v-for="(line, index) in lines" :key="index" class="line-number">{{ index + 1 }}</div>
      </div>
      <div class="editor-wrapper">
        <textarea
          ref="textareaRef"
          v-model="content"
          class="perl-textarea"
          :placeholder="placeholder"
          spellcheck="false"
          @input="handleInput"
        ></textarea>
        <div class="highlight-layer" v-html="highlightedContent"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import hljs from 'highlight.js/lib/core'
import perl from 'highlight.js/lib/languages/perl'

hljs.registerLanguage('perl', perl)

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const content = ref(props.modelValue)

const lines = computed(() => content.value.split('\n'))

const highlightedContent = computed(() => {
  if (!content.value.trim()) return ''
  try {
    const result = hljs.highlight(content.value, { language: 'perl' })
    return result.value
  } catch {
    return escapeHtml(content.value)
  }
})

const escapeHtml = (str: string): string => {
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

const handleInput = () => {
  emit('update:modelValue', content.value)
  syncScroll()
}

const syncScroll = () => {
  if (!textareaRef.value) return
  const highlightLayer = textareaRef.value.parentElement?.querySelector('.highlight-layer')
  if (highlightLayer) {
    highlightLayer.scrollTop = textareaRef.value.scrollTop
    highlightLayer.scrollLeft = textareaRef.value.scrollLeft
  }
}

const formatCode = () => {
  let code = content.value
  code = code.replace(/\s+/g, ' ')
  code = code.replace(/;/g, ';\n')
  code = code.replace(/\{/g, ' {\n')
  code = code.replace(/\}/g, '\n}')
  code = code.trim()
  content.value = code
  emit('update:modelValue', content.value)
}

const clearContent = () => {
  content.value = ''
  emit('update:modelValue', '')
}

watch(() => props.modelValue, (newVal) => {
  if (newVal !== content.value) {
    content.value = newVal
  }
})

onMounted(() => {
  nextTick(() => {
    syncScroll()
  })
})
</script>

<style scoped>
.perl-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.perl-editor-toolbar {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e8e8e8;
}

.perl-editor-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.line-numbers {
  width: 50px;
  background: #2d2d2d;
  padding: 12px 0;
  text-align: right;
  border-right: 1px solid #3d3d3d;
  overflow-y: auto;
  flex-shrink: 0;
}

.line-number {
  font-size: 13px;
  line-height: 20px;
  color: #858585;
  padding-right: 8px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.editor-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.perl-textarea {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 20px;
  resize: none;
  background: transparent;
  color: transparent;
  caret-color: #fff;
  z-index: 1;
}

.perl-textarea::placeholder {
  color: #6b7280;
}

.highlight-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 20px;
  background: #1e1e1e;
  overflow-y: auto;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  pointer-events: none;
  z-index: 0;
}

.highlight-layer :deep(.hljs-keyword) {
  color: #569cd6;
}

.highlight-layer :deep(.hljs-string) {
  color: #ce9178;
}

.highlight-layer :deep(.hljs-number) {
  color: #b5cea8;
}

.highlight-layer :deep(.hljs-comment) {
  color: #6a9955;
  font-style: italic;
}

.highlight-layer :deep(.hljs-function) {
  color: #dcdcaa;
}

.highlight-layer :deep(.hljs-title) {
  color: #dcdcaa;
}

.highlight-layer :deep(.hljs-params) {
  color: #9cdcfe;
}

.highlight-layer :deep(.hljs-built_in) {
  color: #4ec9b0;
}

.highlight-layer :deep(.hljs-attr) {
  color: #9cdcfe;
}

.highlight-layer :deep(.hljs-variable) {
  color: #9cdcfe;
}
</style>