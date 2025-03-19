/**
 * Verifier service functionality
 */
const { createApiClient, validateRequiredFields } = require('../utils');
const QRCode = require('qrcode');

class VerifierService {
  constructor(options = {}) {
    this.client = createApiClient(options);
  }
  
  /**
   * Validate a presented credential
   * @param {Object} credential - The credential to validate
   * @returns {Promise<Object>} Validation result
   */
  async validateCredential(credential) {
    if (!credential) {
      throw new Error('Credential is required');
    }
    
    try {
      return await this.client.post('/api/v1/verify', { credential });
    } catch (error) {
      console.error('Failed to validate credential:', error);
      throw error;
    }
  }
  
  /**
   * Generate QR code for credential offering
   * @param {Object} params - QR code parameters
   * @param {String} params.offeringUrl - The offering URL
   * @returns {Promise<String>} Base64 encoded QR code image
   */
  async generateQRCode({ offeringUrl }) {
    if (!offeringUrl) {
      throw new Error('Offering URL is required');
    }
    
    try {
      // Generate QR code as base64 encoded image
      return await QRCode.toDataURL(offeringUrl, {
        errorCorrectionLevel: 'H',
        margin: 1,
        width: 300,
        color: {
          dark: '#000000',
          light: '#ffffff'
        }
      });
    } catch (error) {
      console.error('Failed to generate QR code:', error);
      throw error;
    }
  }
  
  /**
   * Create a validator credential offering
   * @param {Object} params - Validator parameters
   * @param {String} params.validatorAddress - Validator address
   * @param {String} [params.validatorName] - Optional validator name
   * @param {String} [params.networkId] - Optional network ID
   * @returns {Promise<Object>} Offering with QR code
   */
  async createValidatorCredential({ validatorAddress, validatorName, networkId = 'mainnet' }) {
    if (!validatorAddress) {
      throw new Error('Validator address is required');
    }
    
    try {
      const credentialSubject = {
        validatorAddress,
        networkId
      };
      
      if (validatorName) {
        credentialSubject.validatorName = validatorName;
      }
      
      // Create open offering for validator credential
      const offering = await this.client.post('/api/v1/offering', {
        credential_type: 'ValidatorCredential',
        credential_subject: credentialSubject
      });
      
      // Generate QR code for the offering URL
      const qrCodeBase64 = await this.generateQRCode({ offeringUrl: offering.url });
      
      return {
        offering_id: offering.id,
        offering_url: offering.url,
        qr_code_base64: qrCodeBase64,
        credential_subject: credentialSubject
      };
    } catch (error) {
      console.error('Failed to create validator credential:', error);
      throw error;
    }
  }
}

module.exports = new VerifierService();