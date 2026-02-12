<template>
  <div class="love-master-container">
    <div class="header">
      <div class="back-button" @click="goBack">Return</div>
      <h1 class="title">AI Guru of love </h1>
      <div class="chat-id">Session ID: {{ chatId }}</div>
    </div>
    
    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom 
          :messages="messages" 
          :connection-status="connectionStatus"
          ai-type="love"
          @send-message="sendMessage"
        />
      </div>
    </div>
    
    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithLoveApp } from '../api'

// Set the page title and metadata
useHead({
  title: 'AI Love Guru AI Super Agent Application Platform',
  meta: [
    {
      name: 'description',
      content: 'AI Love Master is a professional emotional consultant of the AI super intelligence application platform, helping you answer various love questions and provide emotional advice'
    },
    {
      name: 'keywords',
      content: 'AI Love Guru, Emotional Counselor,Love Counseling,AI Chat,Emotional Problems,AI Agent'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

// Generate random session IDs
const generateChatId = () => {
  return 'love_' + Math.random().toString(36).substring(2, 10)
}

// Add a message to the list
const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime()
  })
}

// Send A Message
const sendMessage = (message) => {
  addMessage(message, true)
  
  // Connect to SSE
  if (eventSource) {
    eventSource.close()
  }
  
  // Create an empty AI reply message
  const aiMessageIndex = messages.value.length
  addMessage('', false)
  
  connectionStatus.value = 'connecting'
  eventSource = chatWithLoveApp(message, chatId.value)
  
  // Listen to SSE messages
  eventSource.onmessage = (event) => {
    const data = event.data
    if (data && data !== '[DONE]') {
      // Update the latest AI message content instead of creating new ones
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }
    
    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }
  
  // Listen for SSE errors
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
  }
}

// Return to the home page
const goBack = () => {
  router.push('/')
}

// Add a welcome message when the page loads
onMounted(() => {
  // Generate a chat ID
  chatId.value = generateChatId()
  
  // Add a welcome message
  addMessage('Welcome to AI Love Master, please tell me about your love problems, I will try my best to help and give advice。', false)
})

// Close the SSE connection before the component is destroyed
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.love-master-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #fff9f9;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #ff6b8b;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-button {
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: opacity 0.2s;
}

.back-button:hover {
  opacity: 0.8;
}

.back-button:before {
  content: '←';
  margin-right: 8px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.chat-id {
  font-size: 14px;
  opacity: 0.8;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.chat-area {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  position: relative;
  /* Set a minimum height to ensure that the content is displayed properly */
  min-height: calc(100vh - 56px - 180px); /* 100vhSubtract the head height and footer height*/
  margin-bottom: 16px; /* Leave space for the footer */
}

.footer-container {
  margin-top: auto;
}

/* Responsive styling */
@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }
  
  .title {
    font-size: 18px;
  }
  
  .chat-id {
    font-size: 12px;
  }
  
  .chat-area {
    padding: 12px;
    min-height: calc(100vh - 48px - 160px); /* Adjust the calculated value */
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
  }
  
  .back-button {
    font-size: 14px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .chat-id {
    display: none;
  }
  
  .chat-area {
    padding: 8px;
    min-height: calc(100vh - 42px - 150px); /* Adjust the calculated value again*/
    margin-bottom: 8px;
  }
}
</style> 