



class Session{
	constructor(wsUri){
		this.wsUri = wsUri;
		this.sessionId =null;
	}

    //Here we send message to server
	sendMessage(e){
		
		//see https://developer.mozilla.org/en-US/docs/Web/API/WebSocket#Ready_state_constants
		console.assert(this.websocket.readyState == 1,"WebSocket is not open but in state:"+this.websocket.readyState);

		
		if(!this.sessionId){
			console.info("Wont send message as sessionId is not set");
			return;
		}

		var x = e.data;
		//Add session id to all messages
		x.sessionId=this.sessionId;			
		

		if(this.websocket){
			this.websocket.send(x.p+JSON.stringify(x));
		}
		else{
			console.log("No websocket");
		}
	}

	

	onClose(e){
		console.log("onClose"+e);
		var msg = {};
		msg.socketInfo = "close";
		postMessage(msg);
	}

	onOpen(e){
		var msg = {};
		msg.socketInfo = "open";
		postMessage(msg);
	}


	onServerMessage(msg){		
		var x =JSON.parse(msg.data);



        var cmd = x.message;//Type of message
        var msg = x.data;//Payload

        console.log("Received message of type:"+cmd);

		if(cmd=="AssignSession"){
			this.sessionId = msg.sessionId;
			console.log("Got loginOk sessionId is "+this.sessionId);
		}	
		if(this.sessionId){	
			postMessage(x);//Ignore message if we still havent logged in ok
		}
		
	}

	onError(msg){
		console.log("onError"+msg);
	}

	connect(){
		this.websocket = new WebSocket(this.wsUri);

		var me = this;

		/*
		var websocket = this.websocket;
		this.websocket.onopen = function(evt){
			//me.onOpen(evt);
			//websocket.send(JSON.stringify(msg));
		};*/

		this.websocket.onopen= e=>me.onOpen(e);
		this.websocket.onclose = e=>me.onClose(e);
		this.websocket.onmessage = e=>me.onServerMessage(e);
		this.websocket.onerror = e=>me.onError(e);
		
	}

	close(){
	    if(this.websocket){
	        this.websocket.close();
	    }
	}
}

var session;

onmessage = function(e){
	console.log("Game script sent:"+JSON.stringify(e.data));
	if(e.data.cmd == "connect"){
		var connectInfo = e.data.connectInfo;
		session = new Session(connectInfo.wsUri);
        session.connect();
	}
	else if(e.data.cmd=="close"){
	    if(session)session.close();
	    this.close();
	}
	else{
		session.sendMessage(e);
	}
};




