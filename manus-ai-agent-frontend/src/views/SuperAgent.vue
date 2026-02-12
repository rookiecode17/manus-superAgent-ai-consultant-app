<template>
  <div class="super-agent-container">
    <div class="header">
      <div class="back-button" @click="goBack">Back</div>
      <h1 class="title">AI super agent</h1>
      <div class="placeholder"></div>
    </div>
    
    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom 
          :messages="messages" 
          :connection-status="connectionStatus"
          ai-type="super"
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
import { chatWithManus } from '../api'

// Set the page title and metadata
useHead({
  title: 'AI Super Agent - Fishskin AI Super Agent Application Platform',
  meta: [
    {
      name: 'description',
      content: 'AI superintelligence is an all-round assistant for the AI superintelligence application platform, which can answer various professional questions and provide accurate suggestions and solutions'
    },
    {
      name: 'keywords',
      content: 'AI super agent, intelligent assistant, professional Q&A, AI Q&A, professional advice, AI agent'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

// Add a message to the list
const addMessage = (content, isUser, type = '') => {
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime()
  })
}

// Send a message
const sendMessage = (message) => {
  addMessage(message, true, 'user-question')
  
  // Connect to SSE
  if (eventSource) {
    eventSource.close()
  }
  
  // Set the connection status
  connectionStatus.value = 'connecting'
  
  // Temporary storage
  let messageBuffer = []; // A buffer used to store SSE messages
  let lastBubbleTime = Date.now(); // The time of creation of the previous bubble
  let isFirstResponse = true; // Whether it is the first response
  
  const chineseEndPunctuation = ['。', '！', '？', '…']; // Punctuation at the end of Chinese sentences
  const minBubbleInterval = 800; // Bubble minimum interval time (milliseconds)
  
  // A function that creates a message bubble
  const createBubble = (content, type = 'ai-answer') => {
    if (!content.trim()) return;
    
    // Add appropriate delays to make the message appear more naturally
    const now = Date.now();
    const timeSinceLastBubble = now - lastBubbleTime;
    
    if (isFirstResponse) {
      // The first message appears immediately
      addMessage(content, false, type);
      isFirstResponse = false;
    } else if (timeSinceLastBubble < minBubbleInterval) {
      // If the interval from the previous bubble is too short, add a delay
      setTimeout(() => {
        addMessage(content, false, type);
      }, minBubbleInterval - timeSinceLastBubble);
    } else {
      // Add messages normally
      addMessage(content, false, type);
    }
    
    lastBubbleTime = now;
    messageBuffer = []; // Empty the buffer
  };
  
  eventSource = chatWithManus(message)
  
  // Listen to SSE messages
  eventSource.onmessage = (event) => {
    const data = event.data
    
    if (data && data !== '[DONE]') {
      messageBuffer.push(data);
      
      // Check if a new bubble should be created
      const combinedText = messageBuffer.join('');
      
      // End of sentence or message length reaches threshold
      const lastChar = data.charAt(data.length - 1);
      const hasCompleteSentence = chineseEndPunctuation.includes(lastChar) || data.includes('\n\n');
      const isLongEnough = combinedText.length > 40;
      
      if (hasCompleteSentence || isLongEnough) {
        createBubble(combinedText);
      }
    }
    
    if (data === '[DONE]') {
      // If there is still something not shown, create the last bubble
      if (messageBuffer.length > 0) {
        const remainingContent = messageBuffer.join('');
        createBubble(remainingContent, 'ai-final');
      }
      
      // Close the connection when you're done
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }
  
  // Listen for SSE errors
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
    
    // If there is something that is not displayed when the error occurs, bubbles are also created
    if (messageBuffer.length > 0) {
      const remainingContent = messageBuffer.join('');
      createBubble(remainingContent, 'ai-error');
    }
  }
}

// Return to the home page
const goBack = () => {
  router.push('/')
}

// Add a welcome message when the page loads
onMounted(() => {
  // Add a welcome message
  addMessage("Hello, I'm an AI super agent. I can answer all kinds of questions and provide professional advice, can there be anything that can help you?", false)
})

// Close the SSE connection before the component is destroyed
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.super-agent-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f9fbff;
}

.header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 16px 24px;
  background-color: #3f51b5;
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
  justify-self: start;
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
  text-align: center;
  justify-self: center;
}

.placeholder {
  width: 1px;
  justify-self: end;
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
  /* 设置最小高度确保内容显示正常 */
  min-height: calc(100vh - 56px - 180px); /* 100vh减去头部高度和页脚高度 */
  margin-bottom: 16px; /* 为页脚留出空间 */
}

.footer-container {
  margin-top: auto;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }
  
  .title {
    font-size: 18px;
  }
  
  .chat-area {
    padding: 12px;
    min-height: calc(100vh - 48px - 160px); /* 调整计算值 */
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
  
  .chat-area {
    padding: 8px;
    min-height: calc(100vh - 42px - 150px); /* 再次调整计算值 */
    margin-bottom: 8px;
  }
}
</style> 