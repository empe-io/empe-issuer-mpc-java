/**
 * Schema management functionality
 */
const { createApiClient, validateRequiredFields } = require('../utils');
const config = require('config');

class SchemaService {
  constructor(options = {}) {
    this.client = createApiClient(options);
    this.schemaTemplates = config.get('schemaTemplates');
  }
  
  /**
   * Create a new credential schema
   * @param {Object} params - Schema parameters
   * @param {String} params.name - Schema name
   * @param {String} params.type - Schema type
   * @param {Object} params.properties - Schema properties
   * @param {Array} params.requiredFields - Required fields
   * @returns {Promise<Object>} Created schema
   */
  async createSchema({ name, type, properties, requiredFields }) {
    if (!validateRequiredFields({ name, type, properties, requiredFields }, 
                               ['name', 'type', 'properties', 'requiredFields'])) {
      throw new Error('Missing required schema parameters');
    }
    
    // Construct the schema structure based on API requirements
    const credentialSubject = {
      type: 'object',
      properties,
      required: requiredFields
    };
    
    const requestBody = {
      name,
      type,
      credentialSubject
    };
    
    try {
      return await this.client.post('/api/v1/schema', requestBody);
    } catch (error) {
      console.error('Failed to create schema:', error);
      throw error;
    }
  }
  
  /**
   * Get all schemas
   * @returns {Promise<Array>} List of schemas
   */
  async getAllSchemas() {
    try {
      return await this.client.get('/api/v1/schema');
    } catch (error) {
      console.error('Failed to get schemas:', error);
      throw error;
    }
  }
  
  /**
   * Get schema by ID
   * @param {String} id - Schema ID
   * @returns {Promise<Object>} Schema data
   */
  async getSchemaById(id) {
    if (!id) {
      throw new Error('Schema ID is required');
    }
    
    try {
      return await this.client.get(`/api/v1/schema/${id}`);
    } catch (error) {
      console.error(`Failed to get schema with ID ${id}:`, error);
      throw error;
    }
  }
  
  /**
   * Delete schema by ID
   * @param {String} id - Schema ID
   * @returns {Promise<void>}
   */
  async deleteSchema(id) {
    if (!id) {
      throw new Error('Schema ID is required');
    }
    
    try {
      await this.client.delete(`/api/v1/schema/${id}`);
    } catch (error) {
      console.error(`Failed to delete schema with ID ${id}:`, error);
      throw error;
    }
  }
  
  /**
   * Check if schema exists by type
   * @param {String} type - Schema type
   * @returns {Promise<Boolean>} True if schema exists
   */
  async schemaExistsByType(type) {
    if (!type) {
      throw new Error('Schema type is required');
    }
    
    try {
      const schemas = await this.getAllSchemas();
      return schemas.some(schema => schema.type === type);
    } catch (error) {
      console.error(`Failed to check if schema type ${type} exists:`, error);
      throw error;
    }
  }
  
  /**
   * Get latest schema version by type
   * @param {String} type - Schema type
   * @returns {Promise<Object|null>} Latest schema version or null if not found
   */
  async getLatestSchemaByType(type) {
    if (!type) {
      throw new Error('Schema type is required');
    }
    
    try {
      const schemas = await this.getAllSchemas();
      const filteredSchemas = schemas.filter(schema => schema.type === type);
      
      if (filteredSchemas.length === 0) {
        return null;
      }
      
      // Sort by version in descending order
      return filteredSchemas.sort((a, b) => b.version - a.version)[0];
    } catch (error) {
      console.error(`Failed to get latest schema by type ${type}:`, error);
      throw error;
    }
  }
}

module.exports = new SchemaService();