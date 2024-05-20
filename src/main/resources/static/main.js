document.getElementById('connectButton').addEventListener('click', () => {
    connectToWebSocket();
});

function connectToWebSocket() {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);
        const userId = '1'; // replace with dynamic user ID if needed
        stompClient.subscribe(`/org/${userId}/push`, (message) => {
            showNotification(message.body);
        });
    });

    stompClient.debug = (str) => {
        console.log(str);
    };

    stompClient.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    };
}

function showNotification(message) {
    const notificationsDiv = document.getElementById('notifications');
    if (notificationsDiv) {
        const notification = document.createElement('div');
        notification.textContent = message;
        notificationsDiv.appendChild(notification);
    }
}
