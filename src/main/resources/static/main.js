document.getElementById('connectButton').addEventListener('click', async () => {
    connectToWebSocket();

    async function connectToWebSocket() {
        try {
            const accessToken = localStorage.getItem('accessToken'); // Function to get a JWT token
            console.log("Obtained Access Token:", accessToken);
            const decodedToken = jwt_decode(accessToken);
            const userId = decodedToken.userId;  // Extract userId from the token
            console.log("Decoded UserId:", userId);

            const socket = new SockJS('http://localhost:8080/ws');
            const stompClient = Stomp.over(socket);

            const headers = {
                'Authorization': `Bearer ${accessToken}`
            };

            stompClient.connect(headers, (frame) => {
                console.log('Connected: ' + frame);
                stompClient.subscribe(`/user/${userId}/push`, (message) => {
                    showNotification(message.body);
                });
            }, (error) => {
                console.error('Connection error: ' + error);
            });

            stompClient.debug = (str) => {
                console.log(str);
            };

            stompClient.onStompError = (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            };
        } catch (error) {
            console.error("Error connecting to WebSocket:", error);
        }
    }


    // async function obtainAccessToken() {
    //     // Make a POST request to the /refresh-token endpoint to get a new access token
    //     const refreshToken = localStorage.getItem('refreshToken');
    //     console.log(refreshToken)
    //     const response = await fetch('http://localhost:8080/v1/auth/refresh-token', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'text/plain'
    //         },
    //         body: `Bearer ${refreshToken}`,
    //         // credentials: 'include'  // Include cookies in the request
    //     });
    //     console.log(response)
    //
    //     if (!response.ok) {
    //         throw new Error('Failed to refresh access token');
    //     }
    //
    //     const data = await response.json();
    //     localStorage.setItem('accessToken', data.accessToken);
    //     console.log(data.accessToken);
    //     return data.accessToken;
    // }

    function showNotification(message) {
        const notificationsDiv = document.getElementById('notifications');
        if (notificationsDiv) {
            const notification = document.createElement('div');
            notification.textContent = message;
            notificationsDiv.appendChild(notification);
        }
    }
})