
// VARIABLES

var username = sessionStorage.getItem('username');
var jwt = sessionStorage.getItem('jwt');

const client = new StompJs.Client();
client.brokerURL = 'ws://localhost:8088/ws';

var connected=false;
var subscriptions = []

shipping_notification = {
    origin: null,
    shippingId: null,
    artifactId: null,
    artifactResource: null,
    accessToken: null
}

var button_id = 0;
const accept_id = 'accept-';
const reject_id = 'reject-';

// CALLBACKS

var onShippingRequest = function(message){
    var shippingRequestObject = JSON.parse(message.body);
    console.log('Shipping Request: '+shippingRequestObject)
    shipping_notification = shippingRequestObject;
    addMessage(shipping_notification,shipping_notification.shippingId)
};

var onShippingResponse = function(message){
    var accepted = message.body
    console.log('Shipping Response: '+accepted);
};

var acceptRequestCallback = function(){
    const body_ = JSON.stringify(
        {
            shippingId:shipping_notification.shippingId,
            accessToken:shipping_notification.accessToken
        });
    client.publish({destination: '/app/shipping/accept', body: body_});
    invalidateShippingNotification();
}

var rejectRequestCallBack = function(){
    invalidateShippingNotification();
}

function invalidateShippingNotification(){
    var accept_button1 = document.getElementById(accept_id+shipping_notification.shippingId);
    accept_button1.style.backgroundColor="red";
    accept_button1.disabled=true
    var reject_button1 = document.getElementById(reject_id+shipping_notification.shippingId);
    reject_button1.style.backgroundColor="blue";
    reject_button1.disabled=true;
}

var onSuccessfulConnection = function(frame){
    console.log('WebSocket connection throught STOMP successful')
    var subscription = client.subscribe("/user/queue/shipping/couriers",onShippingRequest)
    subscriptions.push(subscription)
    var subscription2 = client.subscribe("/user/queue/shipping/response",onShippingResponse)
    subscriptions.push(subscription2)
    updateViewOnConnection()
};

var onFailedConnection = function(frame){
    console.log('Stomp ERROR: '+frame.body);
    alert(error.headers)
};

// Config Client

client.onConnect = onSuccessfulConnection;
  
client.onStompError = onFailedConnection;



// FUNCTIONS

function updateViewOnConnection(){
    console.log('On Connection View Update')
    var connect_button = document.getElementById('connect_button')
    var share_button = document.getElementById('share_loc_btn')
    if(connected==true){
        connect_button.innerHTML='Connect'
        connect_button.style.backgroundColor='black'
        share_button.disabled=true
        connected=false;
    }else{
        connect_button.innerHTML='Disconnect'
        connect_button.style.backgroundColor='blue'
        share_button.disabled=false
        connected=true;
    }
}

function onConnectButtonClicked(){
    console.log('Sending CONNECT frame')
    if(connected==false){
        client.activate()
    }else{
        console.log('Closing connection...')
        updateViewOnConnection()
        for(let i=0; i<subscriptions.length; i++){
            let sub = subscriptions[i]
            sub.unsubscribe()
        }
        client.deactivate();
    }
}


function sharePosition(){
    var latitude_ = document.geolocation_form.latitude.value;
    var longitude_ = document.geolocation_form.longitude.value;
    if(latitude_.length==0 || longitude_.length==0){
        alert('You must fill all fields of the form before sending to the server')
        return;
    }
    const body_ = JSON.stringify({latitude:latitude_,longitude:longitude_});
    client.publish({destination: '/app/shipping/position/share', body: body_});
}

function acceptRequest(){
    const body_ = JSON.stringify({shippingId:shippingId_, accessToken: accessToken_});
    client.publish({destination: '/app/shipping/accept', body: body_});
}

function addMessage(shipping_request,shippingId){
    button_id = button_id + 1;
    var message = document.createElement("div");
    message.className="message"
    var image = document.createElement("img");
    image.className="message_img"
    image.setAttribute('src', 'img/storeic.svg');
    var message_info = document.createElement("div");
    message_info.className="message_info"
    var store_title = document.createElement("h5");
    var store_title_text = document.createTextNode(shipping_request.origin.owner);
    store_title.appendChild(store_title_text);
    var store_address = document.createElement("p");
    var store_address_text = document.createTextNode(shipping_request.origin.address);
    store_address.appendChild(store_address_text);
    var accept_button = document.createElement("button");
    accept_button.setAttribute('id',accept_id+shippingId);
    accept_button.onclick=acceptRequestCallback;
    var accept_button_text = document.createTextNode("accept");
    accept_button.appendChild(accept_button_text)
    accept_button.className="message_accept_button"
    var reject_button = document.createElement("button");
    var reject_button_text = document.createTextNode("reject");
    reject_button.setAttribute('id',reject_id+shippingId);
    reject_button.onclick=rejectRequestCallBack;
    reject_button.appendChild(reject_button_text)
    reject_button.className="message_reject_button"
    // Build the tree
    message.appendChild(image);
    message_info.appendChild(store_title);
    message_info.appendChild(store_address);
    message.appendChild(message_info);
    message.appendChild(accept_button);
    message.appendChild(reject_button);
    // Add to the messages container
    var messages = document.getElementById("messages");
    var space = document.createElement("div");
    space.className="message_space";
    messages.appendChild(space);
    messages.appendChild(message);
}