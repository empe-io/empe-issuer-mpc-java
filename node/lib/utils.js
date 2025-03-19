/**
 * Common utility functions
 */
const axios = require('axios');
const config = require('config');

/**
 * Creates an API client with default configuration
 * @param {Object} options - Custom options for the API client
 * @returns {Object} Configured axios instance
 */
function createApiClient(options = {}) {
  const baseUrl = options.baseUrl || config.get('api.baseUrl');
  const clientSecret = options.clientSecret || config.get('api.clientSecret');
  
  const client = axios.create({
    baseURL: baseUrl,
    headers: {
      'Content-Type': 'application/json',
      'x-client-secret': clientSecret
    },
    timeout: 10000
  });
  
  // Add response interceptor for error handling
  client.interceptors.response.use(
    response => response.data,
    error => {
      const enhancedError = new Error(
        error.response?.data?.error || error.message || 'Unknown error'
      );
      enhancedError.status = error.response?.status || 500;
      enhancedError.details = error.response?.data?.details;
      throw enhancedError;
    }
  );
  
  return client;
}

/**
 * Validates if all required fields are present
 * @param {Object} data - Data object to validate
 * @param {Array} requiredFields - Array of required field names
 * @returns {Boolean} True if all required fields are present
 */
function validateRequiredFields(data, requiredFields) {
  if (!data || typeof data !== 'object') {
    return false;
  }
  
  return requiredFields.every(field => {
    return Object.prototype.hasOwnProperty.call(data, field) && 
           data[field] !== null && 
           data[field] !== undefined && 
           data[field] !== '';
  });
}

module.exports = {
  createApiClient,
  validateRequiredFields
};