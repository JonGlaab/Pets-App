initialize();

async function initialize() {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const sessionId = urlParams.get('session_id');
    
    if (!sessionId) {
        console.error('No session ID found in URL');
        return;
    }

    try {
        const response = await fetch(`/stripe/session-status?session_id=${sessionId}`);
        const session = await response.json();

        if (session.error) {
            console.error('Error retrieving session:', session.error);
            return;
        }

        if (session.status === 'open') {
           
            window.location.replace('/stripe/checkout');
        } else if (session.status === 'complete') {
            
            console.log('Payment completed successfully');
           
        } else {
            console.log('Session status:', session.status);
        }
    } catch (error) {
        console.error('Error initializing return page:', error);
    }
}