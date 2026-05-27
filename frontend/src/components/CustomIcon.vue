<template>
  <svg 
    :width="size || 18" 
    :height="size || 18" 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    stroke-width="2" 
    stroke-linecap="round" 
    stroke-linejoin="round"
    :class="className"
  >
    <use :xlink:href="`/icons/${name}.svg#${name}`" />
  </svg>
</template>

<script setup lang="ts">
defineProps<{
  name: string
  size?: number
  className?: string
}>()
</script>