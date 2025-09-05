<template>
  <div class="editor-container">
    <header class="editor-header">
      <h1>PKES Save Editor</h1>
      <button @click="handleSave" class="save-button">Download</button>
    </header>

    <div v-if="error" class="error-message">
      <p>Failed to load the save data.</p>
      <pre>{{ error }}</pre>
    </div>

    <div v-if="saveData" class="data-root">
      <DataNode node-key="saveData" v-model="saveData" />
    </div>

    <div v-else class="loading-message">
      <p>Loading save data...</p>
    </div>
  </div>
</template>

<script>
import DataNode from './DataNode.vue';

export default {
  name: 'EditorView',
  components: {
    DataNode
  },
  data() {
    return {
      saveData: null,
      error: null
    };
  },
  created() {
    try {
      const dataString = sessionStorage.getItem('saveData');
      if (dataString) {
        this.saveData = JSON.parse(dataString);
      }
    } catch (e) {
      console.error("Failed to parse save data:", e);
      this.error = e.message;
    }
  },
  methods: {
    handleSave() {
      if (!this.saveData) {
        alert("There's no data to modify.");
        return;
      }
      const dataStr = JSON.stringify(this.saveData, null, 2);
      const blob = new Blob([dataStr], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = "edited_save.json";
      link.click();
      URL.revokeObjectURL(url);
    }
  }
}
</script>

<style scoped>
.editor-container { padding: 2rem; max-width: 900px; margin: 0 auto; }
.editor-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; border-bottom: 2px solid #f0f0f0; padding-bottom: 1rem; }
.save-button { background-color: #4a90e2; color: white; border: none; padding: 0.75rem 1.5rem; border-radius: 5px; font-size: 1rem; cursor: pointer; transition: background-color 0.3s; }
.save-button:hover { background-color: #357abd; }
.data-root { background-color: #fafafa; padding: 1.5rem; border-radius: 8px; border: 1px solid #e0e0e0; overflow-x: auto; }
.error-message, .loading-message { text-align: center; color: #888; margin-top: 4rem; }
</style>