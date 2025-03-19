/**
 * Issuer API routes
 */
const express = require('express');
const router = express.Router();
const { issuerService } = require('../lib/issuer');
const schemaService = require('../lib/schemas');

/**
 * Create a schema
 * 
 * POST /api/v1/issuer/schema
 */
router.post('/schema', async (req, res, next) => {
  try {
    const { name, type, properties, requiredFields } = req.body;
    
    const schema = await schemaService.createSchema({
      name,
      type,
      properties,
      requiredFields
    });
    
    res.status(201).json(schema);
  } catch (error) {
    next(error);
  }
});

/**
 * Get all schemas
 * 
 * GET /api/v1/issuer/schema
 */
router.get('/schema', async (req, res, next) => {
  try {
    const schemas = await schemaService.getAllSchemas();
    res.json(schemas);
  } catch (error) {
    next(error);
  }
});

/**
 * Get schema by ID
 * 
 * GET /api/v1/issuer/schema/:id
 */
router.get('/schema/:id', async (req, res, next) => {
  try {
    const schema = await schemaService.getSchemaById(req.params.id);
    if (!schema) {
      return res.status(404).json({ error: 'Schema not found' });
    }
    res.json(schema);
  } catch (error) {
    next(error);
  }
});

/**
 * Delete schema by ID
 * 
 * DELETE /api/v1/issuer/schema