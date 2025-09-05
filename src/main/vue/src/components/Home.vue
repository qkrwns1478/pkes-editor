<template>
  <div class="home-container">
    <div v-if="errorMessage" class="toast-message">
      {{ errorMessage }}
    </div>

    <header class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">PKES Save Editor</h1>
        <p class="hero-description">
          Web save editor for Pokémon Essentials based games
        </p>
      </div>
    </header>

    <section class="upload-section">
      <div class="upload-container">
        <div
            class="drop-zone"
            :class="{ 'drag-over': isDragOver, 'uploading': isUploading }"
            @drop="handleDrop"
            @dragover="handleDragOver"
            @dragenter="handleDragEnter"
            @dragleave="handleDragLeave"
        >
          <div class="drop-zone-content">
            <div class="upload-icon">
              <svg v-if="!isUploading" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7,10 12,15 17,10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              <div v-else class="loading-spinner"></div>
            </div>

            <div class="upload-text">
              <p v-if="!isUploading" class="primary-text">
                Upload to drag or click here
              </p>
              <p v-else class="primary-text">Uploading...</p>

              <p class="secondary-text">
                <strong>Allowed format:</strong> rxdata
              </p>
              <p class="file-size-text">Max File Size: 10MB</p>
            </div>

            <input
                ref="fileInput"
                type="file"
                class="file-input"
                accept=".rxdata"
                @change="handleFileSelect"
            />

            <button
                class="browse-button"
                @click="triggerFileInput"
                :disabled="isUploading"
            >
              Select File
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'HomePage',
  emits: ['upload-success'],
  setup(props, context) {
    const fileInput = ref(null)
    const isDragOver = ref(false)
    const isUploading = ref(false)
    const errorMessage = ref('')
    let errorTimer = null

    const maxFileSize = 10 * 1024 * 1024 // 10MB

    const handleDragOver = (e) => {
      e.preventDefault()
      isDragOver.value = true
    }

    const handleDragEnter = (e) => {
      e.preventDefault()
      isDragOver.value = true
    }

    const handleDragLeave = (e) => {
      e.preventDefault()
      isDragOver.value = false
    }

    const handleDrop = (e) => {
      e.preventDefault()
      isDragOver.value = false
      const files = Array.from(e.dataTransfer.files)
      if (files.length > 0) {
        processFile(files[0])
      }
    }

    const handleFileSelect = (e) => {
      const files = Array.from(e.target.files)
      if (files.length > 0) {
        processFile(files[0])
      }
    }

    const triggerFileInput = () => {
      fileInput.value?.click()
    }

    const processFile = async (file) => {
      clearError()

      if (!validateFile(file)) {
        fileInput.value.value = ''
        return
      }

      try {
        isUploading.value = true
        await uploadFile(file)
      } catch (error) {
        setError(`Failed to upload.`)
      } finally {
        isUploading.value = false
        fileInput.value.value = ''
      }
    }

    const validateFile = (file) => {
      if (file.size > maxFileSize) {
        setError(`File size is too large.`)
        return false
      }

      const extension = file.name.split('.').pop().toLowerCase()
      if (extension !== 'rxdata') {
        setError(`Please upload rxdata file.`)
        return false
      }

      return true
    }

    const uploadFile = async (file) => {
      const formData = new FormData()
      formData.append('file', file)

      try {
        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData
        })

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Server responded with ${response.status}: ${errorText}`);
        }

        const result = await response.json()
        sessionStorage.setItem('saveData', JSON.stringify(result))
        context.emit('upload-success');
      } catch (error) {
        console.error('Upload failed details:', error);
        throw new Error('Upload failed')
      }
    }

    const setError = (message) => {
      if (errorTimer) {
        clearTimeout(errorTimer)
      }
      errorMessage.value = message
      errorTimer = setTimeout(() => {
        errorMessage.value = ''
      }, 2000)
    }

    const clearError = () => {
      if (errorTimer) {
        clearTimeout(errorTimer)
        errorTimer = null
      }
      errorMessage.value = ''
    }

    return {
      fileInput,
      isDragOver,
      isUploading,
      errorMessage,
      handleDragOver,
      handleDragEnter,
      handleDragLeave,
      handleDrop,
      handleFileSelect,
      triggerFileInput,
      clearError
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.toast-message {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #f8d7da;
  color: #721c24;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  font-weight: 500;
  min-width: 300px;
  text-align: center;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.hero-section {
  padding: 80px 20px;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.hero-description {
  font-size: 1.1rem;
  line-height: 1.6;
  opacity: 0.8;
  max-width: 600px;
  margin: 0 auto;
}

.upload-section {
  background: white;
  padding: 60px 20px;
}

.upload-container {
  max-width: 800px;
  margin: 0 auto;
}

.drop-zone {
  border: 3px dashed #ccc;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  transition: all 0.3s ease;
  background: #fafafa;
  cursor: pointer;
}

.drop-zone:hover,
.drop-zone.drag-over {
  border-color: #667eea;
  background: #f0f4ff;
}

.drop-zone.uploading {
  border-color: #28a745;
  background: #f8fff9;
}

.drop-zone-content {
  position: relative;
}

.upload-icon {
  color: #667eea;
  margin-bottom: 1rem;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.upload-text .primary-text {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.upload-text .secondary-text {
  color: #666;
  margin-bottom: 0.25rem;
}

.upload-text .file-size-text {
  color: #999;
  font-size: 0.9rem;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.browse-button {
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
  transition: background 0.3s ease;
}

.browse-button:hover:not(:disabled) {
  background: #5a6fd8;
}

.browse-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .upload-section {
    padding: 40px 15px;
  }

  .drop-zone {
    padding: 40px 15px;
  }

  .toast-message {
    left: 10px;
    right: 10px;
    transform: none;
    min-width: auto;
  }
}
</style>
