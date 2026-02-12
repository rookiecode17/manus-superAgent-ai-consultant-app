import axios from 'axios'

// Set the API base URL based on the environment variables
const API_BASE_URL = process.env.NODE_ENV === 'production' 
 ? '/api' // The production environment uses relative paths, which are suitable for front-end and back-end deployments under the same domain name
 : 'http://localhost:8123/api' // The development environment points to the local backend service

// Create an axios instance
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000
})

// Encapsulated SSE connections
export const connectSSE = (url, params, onMessage, onError) => {
  // Build URLs with parameters
  const queryString = Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  
  const fullUrl = `${API_BASE_URL}${url}?${queryString}`
  
  // create EventSource
  const eventSource = new EventSource(fullUrl)
  
  eventSource.onmessage = event => {
    let data = event.data
    
    // Check if it is a special mark
    if (data === '[DONE]') {
      if (onMessage) onMessage('[DONE]')
    } else {
      // Handling normal messages
      if (onMessage) onMessage(data)
    }
  }
  
  eventSource.onerror = error => {
    if (onError) onError(error)
    eventSource.close()
  }
  
  // Return the eventSource instance so that the connection can be closed later
  return eventSource
}

// AI Love Guru Chat
export const chatWithLoveApp = (message, chatId) => {
  return connectSSE('/ai/love_app/chat/sse', { message, chatId })
}

//AI super agent chat
export const chatWithManus = (message) => {
  return connectSSE('/ai/manus/chat', { message })
}

export default {
  chatWithLoveApp,
  chatWithManus
} 