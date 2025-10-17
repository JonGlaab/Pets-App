// Initialize Stripe
const stripePublishableKey = document.querySelector("#stripePublishableKey").value;
const stripe = Stripe(stripePublishableKey);

const checkoutButton = document.querySelector("#checkout-button");
const buttonText = document.querySelector("#button-text");
const loadingSpinner = document.querySelector("#loading-spinner");
const errorMessage = document.querySelector("#error-message");
const successMessage = document.querySelector("#success-message");

checkoutButton.addEventListener("click", async () => {
    const adopterEmail = document.querySelector("#adopterEmail").value;
    const petName = document.querySelector("#petName").value;
    const applicationId = document.querySelector("#applicationId").value;


    buttonText.classList.add("d-none");
    loadingSpinner.classList.remove("d-none");
    checkoutButton.disabled = true;
    errorMessage.classList.add("d-none");
    successMessage.classList.add("d-none");

    try {
        const response = await fetch("/stripe/create-checkout-session", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({
                adopterEmail,
                petName,
                applicationId
            })
        });

        const session = await response.json();

        if (session.error) {
            throw new Error(session.error);
        }

        const { error } = await stripe.redirectToCheckout({ 
            sessionId: session.id 
        });

        if (error) {
            throw new Error(error.message);
        }

    } catch (error) {
        console.error("Error:", error);
        
        errorMessage.textContent = `Payment failed: ${error.message}`;
        errorMessage.classList.remove("d-none");
        
        buttonText.classList.remove("d-none");
        loadingSpinner.classList.add("d-none");
        checkoutButton.disabled = false;
    }
});