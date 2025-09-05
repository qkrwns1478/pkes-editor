<template>
  <component :is="currentView" @upload-success="navigateToEditor"></component>
</template>

<script>
import Home from './components/Home.vue';
import EditorView from './components/EditorView.vue';

export default {
  name: 'App',
  components: {
    Home,
    EditorView
  },
  data() {
    return {
      currentView: 'Home'
    };
  },
  methods: {
    navigateToEditor() {
      this.currentView = 'EditorView';
    }
  }
}
</script>

<style>
body {
  margin: 0;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>