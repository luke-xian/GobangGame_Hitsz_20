var chess = document.getElementById("chess");//获取canvas
var context = chess.getContext("2d");
var start_X = 15, start_Y = 15;
var rows = 14;
var chess_size = 30 - 2;
var blank_size = 30;

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


var me = true;
var black_chess = new Image();
var white_chess = new Image();
black_chess.src = "Img/blackStone.gif";
white_chess.src = "Img/whiteStone.gif";

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
//判断当前浏览器是否支持WebSocket
if ('WebSocket' in window) {
    websocket = new WebSocket("ws://localhost:8080/Game/websocket");
}
else {
    alert('当前浏览器 Not support websocket')
}

//连接发生错误的回调方法
websocket.onerror = function () {
    setMessageInnerHTML("WebSocket连接发生错误");
};

//连接成功建立的回调方法
websocket.onopen = function () {
    setMessageInnerHTML("WebSocket连接成功");
}

//接收到消息的回调方法
websocket.onmessage = function (event) {
    setMessageInnerHTML(event.data);
}

//连接关闭的回调方法
websocket.onclose = function () {
    setMessageInnerHTML("WebSocket连接关闭");
}

//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function () {
    closeWebSocket();
}

//将消息显示在网页上
function setMessageInnerHTML(innerHTML) {
    document.getElementById('message').innerHTML += innerHTML + '<br/>';
	var location = innerHTML.split("-");
	document.getElementById('message').innerHTML += location[0] + '<br/>';
	document.getElementById('message').innerHTML += location[1] + '<br/>';
}

//关闭WebSocket连接
function closeWebSocket() {
    websocket.close();
}

//发送消息
function send() {
    var message = document.getElementById('text').value;
    websocket.send(message);
}

var sendstep=function(i,j){
	var message=i+"-"+j;
	websocket.send(message);
}




chess.onclick = function (e) {
    var x = e.offsetX - start_X;
    var y = e.offsetY - start_Y;
    var i = Math.floor(x / 30);
    var j = Math.floor(y / 30);
	
	//webSocket.send(i + "-" + j);
	sendstep(i,j);
	
    if (i >= 0 && j >= 0 && i < rows && j < rows) {
        
		oneStep(i, j, me);
        me = !me;
    }
}
var oneStep = function (i, j, me) {//i,j分别是在棋盘中的定位，me代表白棋还是黑棋
    if (me) {
        context.drawImage(black_chess, start_X + i * blank_size + 1, start_Y + j * blank_size + 1, chess_size, chess_size);
    } else {
        context.drawImage(white_chess, start_X + i * blank_size + 1, start_Y + j * blank_size + 1, chess_size, chess_size);
    }
}



