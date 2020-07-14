var chess = document.getElementById("chess");//获取canvas
var context = chess.getContext("2d");
var start_X = 15, start_Y = 15;
var rows = 15;
var chess_size = 28 ;
var blank_size = 30;
var countline=0;
//var flushcount =0;


// 棋盘状态
var chessBoard = [];
for (var i = 0; i < rows + 1; i++) {
    chessBoard[i] = [];
    for (var j = 0; j < rows + 1; j++) {
        chessBoard[i][j] = 0;
    }
}

// 画棋盘背景
context.strokeStyle = "#000";//画笔的颜色
var back_image = new Image();
back_image.src = "Img/background.gif";
back_image.onload = function () {
    context.drawImage(back_image, 0, 0, chess.clientWidth, chess.clientHeight);
    drawLine();
}


var color = null;
var me=false;
var black_chess = new Image();
var white_chess = new Image();
black_chess.src = "Img/blackStone.gif";
white_chess.src = "Img/whiteStone.gif";
var attack=false;

var yourturn= true;


function drawLine() {//把画线封装成函数
    for (var i = 0; i < rows + 1; i++) {//通过循环画网格
        context.moveTo(start_X, start_Y + i * blank_size);
        context.lineTo(start_X + rows * blank_size, start_Y + i * blank_size);
        context.stroke();
        context.moveTo(start_X + i * blank_size, start_Y);
        context.lineTo(start_X + i * blank_size, start_Y + rows * blank_size);
        context.stroke();
    }
}




var websocket = null;
var nickname=null;

//document.getElementById("connBtn").click(function () {	
//$("connBtn").click(function () {	
function login(){
    if(nickname!=null)return;

	nickname=document.getElementById('nickname').value;
	 if (nickname == null || nickname == '') {
        alert("必须输入昵称");
		location.reload(true);
        return;
    }
	
	document.getElementById('userno').innerHTML = nickname.substring(nickname.length-3,nickname.length);
	document.getElementById('systemshow').innerHTML="正在匹配";
	document.getElementById('nickname').style.display="none";
	document.getElementById('login').style.display="none";
	document.getElementById('onemore').style.display="inline";
	document.getElementById('end').style.display="inline";
	
	//document.getElementById('end').style.margin="0 auto";
	//判断当前浏览器是否支持WebSocket
	if ('WebSocket' in window) {
	    //websocket = new WebSocket("ws://localhost:8080/Game/websocket");
		websocket = new WebSocket("ws:"+window.location.href.split("http:")[1]+"/websocket/"+nickname);
	}
	else {
	    alert('当前浏览器 Not support websocket')
	}
	
	//连接发生错误的回调方法
	websocket.onerror = function () {
	    //setMessageInnerHTML("WebSocket连接发生错误");
		document.getElementById('message').innerHTML += "连接发生错误" + '<br/>';
		$("#message").scrollTop($("#message")[0].scrollHeight);
		//CountLine();
	};
	
	//连接成功建立的回调方法
	websocket.onopen = function () {
	    //setMessageInnerHTML("WebSocket连接成功");
		document.getElementById('message').innerHTML += "连接成功" + '<br/>';
		$("#message").scrollTop($("#message")[0].scrollHeight);
		//CountLine();
	}
	
	//接收到消息的回调方法
	websocket.onmessage = function (event) {
	    setMessageInnerHTML(event.data);
	}
	
	//连接关闭的回调方法
	websocket.onclose = function () {
	    //setMessageInnerHTML("WebSocket连接关闭");
		document.getElementById('message').innerHTML += "连接关闭" + '<br/>';
		$("#message").scrollTop($("#message")[0].scrollHeight);
		//CountLine();
		document.getElementById('message').innerHTML += "重新连接请刷新页面" + '<br/>';
		$("#message").scrollTop($("#message")[0].scrollHeight);
		//CountLine();
	}
	
	//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
	window.onbeforeunload = function () {
		if(!attack){
				    closeWebSocket();
		}
	}
}
// });   

//将消息显示在网页上
function setMessageInnerHTML(innerHTML) {
	
	var status=innerHTML.split("|")[0];
	
	if(status=="message"){
		var temp_message=innerHTML.split("|")[1];
		var temp_color="";
		
		//document.getElementById('message').innerHTML +=temp_message.split("-")[0] + '<br/>';
		//document.getElementById('message').innerHTML +=temp_message + '<br/>';
		
		if(temp_message.search("获胜！")>-1){
			 //temp_color=temp_message.split("-")[1] ;
		     //temp_message=temp_message.split("-")[0];
			//closeWebSocket();
			alert(temp_message);
			/*if(temp_color=="b"){
				alert("白棋"+temp_message+"请刷新页面");
			}else if(temp_color=="w"){
				alert("黑棋"+temp_message+"请刷新页面");
			}*/						
		}
		
		if(temp_message.search("System:")>-1){
			if(temp_message.search("您的回合")>-1){
				document.getElementById('systemshow').innerHTML="您的回合";
				document.getElementById('systemshow').style.background="#ffde2e";
				document.getElementById('systemshow').style.color="#000";
				me=true;
			}else if(temp_message.search("对手的回合")>-1){
				document.getElementById('systemshow').innerHTML="对手回合";
				document.getElementById('systemshow').style.background="#848484";
				document.getElementById('systemshow').style.color="#fff";
				me=false;
			}else if(temp_message.search("执白子")>-1){
				for (var i = 0; i < rows + 1; i++) {
					for (var j = 0; j < rows + 1; j++) {
    				chessBoard[i][j] = 0;
					}
				}
				back_image.onload();
				document.getElementById('chess1').style.background="#fff";
				document.getElementById('chess2').style.background="#000";
			}else if(temp_message.search("执黑子")>-1){
				for (var i = 0; i < rows + 1; i++) {
					for (var j = 0; j < rows + 1; j++) {
    				chessBoard[i][j] = 0;
					}
				}
				back_image.onload();
				document.getElementById('chess1').style.background="#000";
				document.getElementById('chess2').style.background="#fff";
			}else if(temp_message.search("昵称已存在")>-1){
				alert("昵称已存在");
				window.location.href = window.location.href; 
				//attack=true;
				//window.location.reload();
						
			}
			else{
				if(temp_message.search("您的对手是：")>-1){
				var opponent=temp_message.split("您的对手是：")[1];
			    document.getElementById('opponent').innerHTML=opponent.substring(opponent.length-3,opponent.length);
				}
				
				var sender=temp_message.split("-")[0].split(":")[0];
				var content=temp_message.split("-")[0].split(":")[1];
				document.getElementById('message').innerHTML +='<span style="color:red">'+sender+':&nbsp;'+'</span>'+content + '<br/>';
				//document.getElementById('message').innerHTML +=temp_message+ '<br/>';
				$("#message").scrollTop($("#message")[0].scrollHeight);
				//CountLine();
				if(temp_message.search("对手已离线")>-1){
					alert("对手已离线!");
					closeWebSocket();
				}
			}
			
		}else{
			var sender=temp_message.split("-")[0].split(":")[0];
			var content=temp_message.split("-")[0].split(":")[1];
			document.getElementById('message').innerHTML +='<span style="color:#66ccff">'+sender+':'+'</span>'+content + '<br/>';
			//document.getElementById('message').innerHTML +='<span style="color:red">测试</span>'+temp_message.split("-")[0] + '<br/>';
			//document.getElementById('message').innerHTML +=$("#message").scrollTop()+ '<br/>';
			//document.getElementById('message').innerHTML +=$("#message")[0].scrollHeight+ '<br/>';
			$("#message").scrollTop($("#message")[0].scrollHeight);
			//document.getElementById('message').scrollTop(100);
			
			//document.getElementById('message').innerHTML +=$("message").scrollTop()+ '<br/>';
			//CountLine();
		}
		

	}
	

	//document.getElementById('message').innerHTML += window.location.href.split("http:")[1] + '<br/>';
	//var location = innerHTML.split("-");
	//document.getElementById('message').innerHTML += location[0] + '<br/>';
	//document.getElementById('message').innerHTML += location[1] + '<br/>';
	
	if(status=="location"){
		var location=innerHTML.split("|")[1];
		//document.getElementById('message').innerHTML += "刚刚下的位置是" +location+ '<br/>';
		//document.getElementById('message').innerHTML += location + '<br/>';
		
		//CountLine();
		

		var i=location.split("-")[0];
		var j=location.split("-")[1];
		var color=location.split("-")[2];	
		//document.getElementById('message').innerHTML += i+ '<br/>';
		//document.getElementById('message').innerHTML += j+'<br/>';
		//document.getElementById('message').innerHTML += color+'<br/>';
	    if (i >= 0 && j >= 0 && i < rows && j < rows) {
		
	        if(chessBoard[i][j]==0){
				chessBoard[i][j]=1;
				//oneStep(i, j, me);
		        //me = !me;
				oneStep(i, j, color);				
			}
    	}							
	}
}

//关闭WebSocket连接
function closeWebSocket() {
    websocket.close();
	alert("即将刷新页面！");
	location.reload(true);	
}

//发送消息
function send() {
	
    var message = document.getElementById('text').value;
	//message="message|"+nickname+message;
	if(nickname!=null){
		message="message|"+nickname+": "+message;
		websocket.send(message);
	}else{
		/* if(typeof(jQuery)=="undefined"){
                alert("jQuery is not imported");
            }else{
                alert("jQuery is imported");
            }*/
		document.getElementById('message').innerHTML += "请登录！"+ '<br/>';
		$("#message").scrollTop($("#message")[0].scrollHeight);
	}
    
}

var sendstep=function(i,j){
	/*if(me){
		color="b";
	}else{
		color="w";
	}*/
	var message="location|"+i+"-"+j;//+"-"+color;
	websocket.send(message);
}

function CountLine(){
	/*countline++;	
	if(countline>18){
		document.getElementById('message').innerHTML=document.getElementById('message').innerHTML.substr(document.getElementById('message').innerHTML.search("<br>")+4);
	}*/
}

function onemore(){
	/*for (var i = 0; i < rows + 1; i++) {
    	for (var j = 0; j < rows + 1; j++) {
        chessBoard[i][j] = 0;
    	}
	}
	back_image.onload();*/
	//me=false;
	var message="location|onemore";//+"-"+color;
	websocket.send(message);
	//document.getElementById('begin').textContent="再来一局";
}




chess.onclick = function (e) {
    var x = e.offsetX - start_X;
    var y = e.offsetY - start_Y;
    var i = Math.floor(x / 30);
    var j = Math.floor(y / 30);
	
	//webSocket.send(i + "-" + j);
	if(chessBoard[i][j]==0&& me){
		sendstep(i,j);
		me=false;		
	}
}
//var oneStep = function (i, j, me) {//i,j分别是在棋盘中的定位，me代表白棋还是黑棋
var oneStep = function (i, j, color) {//i,j分别是在棋盘中的定位，me代表白棋还是黑棋
    if (color=="b") {
        context.drawImage(black_chess, start_X + i * blank_size + 1, start_Y + j * blank_size + 1, chess_size, chess_size);
    } else if(color=="w"){
        context.drawImage(white_chess, start_X + i * blank_size + 1, start_Y + j * blank_size + 1, chess_size, chess_size);
    }
}



