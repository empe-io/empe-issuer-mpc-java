/**
 * Credential issuing service functionality
 */
const { createApiClient, validateRequiredFields } = require('../utils');

class CredentialIssuingService {
  constructor(options = {}) {
    this.client = createApiClient(options);
  }
  
  /**
   * Create a credential offering
   * @param {Object} params - Offering parameters
   * @param {String} params.type - Credential type
   * @param {Object} params.credentialSubject - Credential subject data
   * @param {String} [params.recipientDid] - Optional recipient DID
   * @returns {Promise<Object>} Created offering
   */
  async createOffering({ type, credentialSubject, recipientDid }) {
    if (!validateRequiredFields({ type, credentialSubject }, ['type', 'credentialSubject'])) {
      throw new Error('Type and credentialSubject are required');
    }
    
    const requestBody = {
      credential_type: type,
      credential_subject: credentialSubject
    };
    
    if (recipientDid) {
      requestBody.recipient = recipientDid;
    }
    
    try {
      return await this.client.post('/api/v1/offering', requestBody);
    } catch (error) {
      console.error('Failed to create offering:', error);
      throw error;
    }
  }
  
  /**
   * Create a targeted credential offering (with specified recipient)
   * @param {Object} params - Offering parameters
   * @param {String} params.type - Credential type
   * @param {Object} params.credentialSubject - Credential subject data
   * @param {String} params.recipientDid - Recipient DID
   * @returns {Promise<Object>} Created offering
   */
  async createTargetedOffering({ type, credentialSubject, recipientDid }) {
    if (!recipientDid) {
      throw new Error('Recipient DID is required for targeted offerings');
    }
    
    return this.createOffering({ type, credentialSubject, recipientDid });
  }
  
  /**
   * Create an open credential offering (available to anyone)
   * @param {Object} params - Offering parameters
   * @param {String} params.type - Credential type
   * @param {Object} params.credentialSubject - Credential subject data
   * @returns {Promise<Object>} Created offering
   */
  async createOpenOffering({ type, credentialSubject }) {
    return this.createOffering({ type, credentialSubject });
  }
  
  /**
   * Initiate DID authentication
   * @param {Object} params - Authentication parameters
   * @param {String} params.recipientDid - Recipient DID to authenticate
   * @returns {Promise<Object>} Authentication challenge
   */
  async initiateDIDAuthentication({ recipientDid }) {
    if (!recipientDid) {
      throw new Error('Recipient DID is required');
    }
    
    const requestBody = {
      did: recipientDid
    };
    
    try {
      return await this.client.post('/api/v1/authorize', requestBody);
    } catch (error) {
      console.error('Failed to initiate DID authentication:', error);
      throw error;
    }
  }
  
  /**
   * Verify DID authentication
   * @param {Object} params - Verification parameters
   * @param {String} params.challenge - Challenge from initiate step
   * @param {String} params.signedChallenge - Signed challenge response
   * @returns {Promise<Object>} Verification result with authorization code
   */
  async verifyDIDAuthentication({ challenge, signedChallenge }) {
    if (!validateRequiredFields({ challenge, signedChallenge }, ['challenge', 'signedChallenge'])) {
      throw new Error('Challenge and signedChallenge are required');
    }
    
    const requestBody = {
      challenge,
      signedChallenge
    };
    
    try {
      return await this.client.post('/api/v1/authorize/verify', requestBody);
    } catch (error) {
      console.error('Failed to verify DID authentication:', error);
      throw error;
    }
  }
  
  /**
   * Exchange authorization code for access token
   * @param {Object} params - Token exchange parameters
   * @param {String} params.authorizationCode - Authorization code
   * @returns {Promise<Object>} Token response
   */
  async exchangeToken({ authorizationCode }) {
    if (!authorizationCode) {
      throw new Error('Authorization code is required');
    }
    
    const requestBody = {
      authorization_code: authorizationCode
    };
    
    try {
      return await this.client.post('/api/v1/connect/token', requestBody);
    } catch (error) {
      console.error('Failed to exchange token:', error);
      throw error;
    }
  }
  
  /**
   * Issue credential
   * @param {Object} params - Issuance parameters
   * @param {String} params.offeringId - Offering ID
   * @param {String} params.accessToken - Access token
   * @returns {Promise<Object>} Issued credential
   */
  async issueCredential({ offeringId, accessToken }) {
    if (!validateRequiredFields({ offeringId, accessToken }, ['offeringId', 'accessToken'])) {
      throw new Error('Offering ID and access token are required');
    }
    
    try {
      const headers = { Authorization: `Bearer ${accessToken}` };
      return await this.client.post(`/api/v1/issue-credential/${offeringId}`, {}, { headers });
    } catch (error) {
      console.error('Failed to issue credential:', error);
      throw error;
    }
  }
}

module.exports = new CredentialIssuingService();