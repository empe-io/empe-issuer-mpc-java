/**
 * Validator credential QR code generator client-side code
 */
document.addEventListener('DOMContentLoaded', function() {
    const generateForm = document.getElementById('validator-form');
    const qrCodeContainer = document.getElementById('qr-code-container');
    const offeringDetails = document.getElementById('offering-details');
    const loadingSpinner = document.getElementById('loading-spinner');
    
    if (generateForm) {
        generateForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            // Show loading spinner
            if (loadingSpinner) {
                loadingSpinner.classList.remove('d-none');
            }
            
            // Clear previous results
            if (qrCodeContainer) {
                qrCodeContainer.innerHTML = '';
            }
            if (offeringDetails) {
                offeringDetails.innerHTML = '';
            }
            
            // Get form data
            const validatorAddress = document.getElementById('validator-address').value;
            
            try {
                // Send request to create validator credential
                const response = await fetch('/api/validator/create', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        validatorAddress: validatorAddress
                    })
                });
                
                const data = await response.json();
                
                // Hide loading spinner
                if (loadingSpinner) {
                    loadingSpinner.classList.add('d-none');
                }
                
                if (data.error) {
                    // Show error
                    if (offeringDetails) {
                        offeringDetails.innerHTML = `
                            <div class="alert alert-danger">
                                Error: ${data.error}
                                ${data.details ? `<br>Details: ${data.details}` : ''}
                            </div>
                        `;
                    }
                } else {
                    // Show QR code
                    if (qrCodeContainer) {
                        const qrImage = document.createElement('img');
                        qrImage.src = data.qr_code_base64;
                        qrImage.alt = 'Validator Credential QR Code';
                        qrImage.classList.add('img-fluid', 'qr-code-image');
                        qrCodeContainer.appendChild(qrImage);
                        
                        // Add download button
                        const downloadBtn = document.createElement('a');
                        downloadBtn.href = data.qr_code_base64;
                        downloadBtn.download = `validator-credential-${data.offering_id}.png`;
                        downloadBtn.classList.add('btn', 'btn-primary', 'mt-3');
                        downloadBtn.textContent = 'Download QR Code';
                        qrCodeContainer.appendChild(downloadBtn);
                    }
                    
                    // Show offering details
                    if (offeringDetails) {
                        offeringDetails.innerHTML = `
                            <div class="card mt-4">
                                <div class="card-header">
                                    <h5>Offering Details</h5>
                                </div>
                                <div class="card-body">
                                    <p><strong>Offering ID:</strong> ${data.offering_id}</p>
                                    <p><strong>Offering URL:</strong> <a href="${data.offering_url}" target="_blank">${data.offering_url}</a></p>
                                </div>
                            </div>
                        `;
                    }
                }
            } catch (error) {
                // Hide loading spinner
                if (loadingSpinner) {
                    loadingSpinner.classList.add('d-none');
                }
                
                // Show error
                if (offeringDetails) {
                    offeringDetails.innerHTML = `
                        <div class="alert alert-danger">
                            An error occurred: ${error.message}
                        </div>
                    `;
                }
            }
        });
    }
});