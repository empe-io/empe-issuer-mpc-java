module.exports = {
  server: {
    port: process.env.PORT || 3000
  },
  api: {
    baseUrl: process.env.API_BASE_URL || 'http://example.com/api/v1',
    clientSecret: process.env.CLIENT_SECRET || ''
  },
  schemaTemplates: {
    // Default schema templates
    validator: {
      name: 'Validator Credential',
      type: 'ValidatorCredential',
      properties: {
        validatorAddress: {
          type: 'string',
          title: 'Validator Address',
          description: 'The blockchain address of the validator',
          format: 'text'
        },
        validatorName: {
          type: 'string',
          title: 'Validator Name',
          description: 'The name of the validator node',
          format: 'text'
        },
        networkId: {
          type: 'string',
          title: 'Network ID',
          description: 'The ID of the blockchain network',
          format: 'text'
        }
      },
      requiredFields: ['validatorAddress', 'networkId']
    }
  }
};