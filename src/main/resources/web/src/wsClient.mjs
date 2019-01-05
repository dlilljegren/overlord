//export default class WsClient{
    class WsClient{   
    constructor(wsUri, onServerMessage){
        this.wsUri = wsUri;
        this.sessionId =null;
        this.onServerMessage = onServerMessage;
    }


    connect(){
		this.websocket = new WebSocket(this.wsUri);

        var me = this;
        
		this.websocket.onopen= e=>me._onOpen(e);
		this.websocket.onclose = e=>me._onClose(e);
		this.websocket.onmessage = e=>me._onServerMessage(e);
		this.websocket.onerror = e=>me._onError(e);
		
    }
    
    sendMessageToServer(data){
        const str =data.p+JSON.stringify(data);
        this.websocket.send(str);
    }

    _onOpen(e){
        this.onServerMessage(["connected"])
        console.info("Socket open "+e);
    }

    _onClose(e){
        console.info("Socket closed "+e);
    }

    _onError(){
        console.error("Error "+e);
    }

    _onServerMessage(msg){
        var x =JSON.parse(msg.data);
        var cmd = x.message;//Type of message
        var payload = x.data;//Payload
        console.log("Received message of type:"+cmd);

		if(cmd=="AssignSession"){
			this.sessionId = payload.sessionId;
			console.info("Got loginOk sessionId is "+this.sessionId);
		}	
		if(this.sessionId){	
            this.onServerMessage([cmd,payload])
		}
    }
}