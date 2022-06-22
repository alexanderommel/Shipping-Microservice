
var url = "ws://localhost:8080/ws/connect";
var client = Stomp.client(url);

function updateMessage(id,status){

}

var onSuccessfulConnection = function(){
    console.log('connection ok')
}

var onFailedConnection = function(error){
    console.log('connection error')
    alert(error.headers.message)
}

function connectToStompServer(){
    var headers = {
        login: 'mylogin',
        passcode: 'mypasscode'
    }
    client.connect(headers,onSuccessfulConnection,onFailedConnection);
}

function sharePosition(position){
    client.send("/shipping/geolocation",{}, JSON.stringify(
        {'latitude':position.latitude,'longitude':position.longitude}
        ));
}


function addMessage(){

    var message = document.createElement("div");
    message.className="message"
    var image = document.createElement("img");
    image.className="message_img"
    image.setAttribute('src', 'img/storeic.svg');
    var message_info = document.createElement("div");
    message_info.className="message_info"
    var store_title = document.createElement("h5");
    var store_title_text = document.createTextNode("Store X");
    store_title.appendChild(store_title_text);
    var store_address = document.createElement("p");
    var store_address_text = document.createTextNode("Calle Oculta 641 y Calle Visible 22");
    store_address.appendChild(store_address_text);
    var accept_button = document.createElement("button");
    var accept_button_text = document.createTextNode("accept");
    accept_button.appendChild(accept_button_text)
    accept_button.className="message_accept_button"
    var reject_button = document.createElement("button");
    var reject_button_text = document.createTextNode("reject");
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