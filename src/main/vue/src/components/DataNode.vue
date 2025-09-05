<template>
  <div class="data-node">
    <details v-if="isObject && !isRef" open>
      <summary class="node-key object-key">{{ nodeKey }}: <span>{...}</span></summary>
      <div class="node-children">
        <DataNode v-for="(value, key) in nodeData" :key="key" :node-key="key" v-model="nodeData[key]" />
      </div>
    </details>

    <details v-else-if="isArray" open>
      <summary class="node-key array-key">{{ nodeKey }}: <span>[...]</span></summary>
      <div class="node-children">
        <DataNode v-for="(item, index) in nodeData" :key="index" :node-key="index" v-model="nodeData[index]" />
      </div>
    </details>

    <div v-else class="node-leaf">
      <label class="node-key primitive-key">{{ nodeKey }}:</label>
      <span v-if="isRef" class="ref-value">↪ Circular Ref ({{ nodeData._class }})</span>
      <input v-else-if="isBoolean" type="checkbox" v-model="editableValue" />
      <input v-else-if="isNumber" type="number" v-model.number="editableValue" class="value-input" />
      <input v-else type="text" v-model="editableValue" class="value-input" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataNode',
  props: {
    modelValue: { required: true },
    nodeKey: { type: [String, Number], required: true }
  },
  emits: ['update:modelValue'],
  computed: {
    editableValue: {
      get() { return this.modelValue; },
      set(value) { this.$emit('update:modelValue', value); }
    },
    nodeData() { return this.modelValue; },
    isObject() { return typeof this.nodeData === 'object' && this.nodeData !== null && !Array.isArray(this.nodeData); },
    isArray() { return Array.isArray(this.nodeData); },
    isRef() {
      // ESLint 규칙을 준수하는 안전한 방식으로 수정
      return this.isObject && Object.prototype.hasOwnProperty.call(this.nodeData, '_ref');
    },
    isBoolean() { return typeof this.nodeData === 'boolean'; },
    isNumber() { return typeof this.nodeData === 'number'; }
  }
}
</script>

<style scoped>
.data-node { font-family: 'Consolas', 'Monaco', monospace; font-size: 14px; }
.node-children { margin-left: 20px; border-left: 1px solid #e0e0e0; padding-left: 10px; }
.node-key { font-weight: bold; color: #4a4a4a; }
.object-key { color: #9b2226; }
.array-key { color: #005f73; }
.primitive-key { color: #555; }
summary { cursor: pointer; padding: 2px 0; }
.node-leaf { display: flex; align-items: center; padding: 4px 0; }
.node-leaf label { margin-right: 8px; }
.value-input { border: 1px solid #ccc; border-radius: 4px; padding: 4px 6px; width: 100%; box-sizing: border-box; }
.ref-value { color: #888; font-style: italic; background-color: #f0f0f0; padding: 4px 6px; border-radius: 4px; }
</style>