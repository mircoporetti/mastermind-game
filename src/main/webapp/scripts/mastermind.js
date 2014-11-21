var sessionId;
var game;

/*Gestione chiusura e refresh*/
window.onbeforeunload = closingCode;

function closingCode(){
   if(game!=null)
	   on_not_finished();
   return null;
}

function on_not_finished(){
	$("body").addClass("wait");
	$.ajax({
		url: '/notFinished',
		method: 'post',
		data: {
			idGame: game.getIdGame,
		},
	}).done(function(){
		$("body").removeClass("wait");
	});
}

/*Caricamento pagina e controllo sessione*/
$(document).ready(function() {
	$("#historyScroll").hide();
	$("#rankScroll").hide();
	$("#loginForm").submit(on_authenticate);
	$("#tryForm").submit(on_try);
	$("#registerForm").submit(on_registrate);
	$("#goRegister").click(on_go_register);
	$("#goLogin").click(on_go_login);
	$("#logout").click(logout);
	$("#newGame").click(on_new_game);
	$("#rank").click(on_rank);
	$("#backToGame").click(on_back_to_game);
	$("#backToRank").click(on_back_to_rank);
	
	check_session();

});

function check_session(){
	$.ajax({
		url: '/checkSession',
		method: 'post',
		success: on_check_session_success,
		error: on_check_session_error,
	});
}


function on_check_session_success(data){
	$("#logout").show();
	$("#rank").show();
	$("#loginRegister").hide();
	sessionId = data.session_id;
	$("#tryForm").hide();
	$("#triesDiv").hide();
	$("#newGame").show();
	$("#backToGame").hide();
	$("#backToRank").hide();
	rateNav(this.sessionId);
}

function on_check_session_error(data){
	$("#suggestionReg").hide();
	$("#registerForm").hide();
	$("#backToGame").hide();
	$("#backToRank").hide();
	$("#game").hide();
	$("#rank").hide();
	$("#labellog").hide();
	$("#logout").hide();
	$("#newGame").hide();
}

/*Login*/
function on_authenticate() {
	$("body").addClass("wait");
	$.ajax({
		url: '/authenticate',
		method: 'post',
		success: on_authenticate_success,
		error: on_authenticate_error,
		data: {
			id: $("#idLogNick").val(),
			password: $("#idLogPass").val()
		},
	}).done(function(){
		$("body").removeClass("wait");
	});
	return false;
}

function on_authenticate_success(data) {
	$("#game").fadeIn(300);
	$("triesDiv").hide();
	$("#backToGame").hide();
	$("#newGame").show();
	$("#rank").show();
	$("#logout").show();
	$("#display").show();
	$("#display").empty();
	$("#tryForm").hide();
	$("#loginRegister").hide();
	$("#display").css("color","green");	
	sessionId = data.session_id;
	rateNav(this.sessionId);
}

function on_authenticate_error(data) {
	$("#display").css("color","red");
	$("#display").text(data.responseJSON.description);
	$("#display").show();
}


/*Creazione nuovo gioco*/
function on_new_game(){
	if (game!=null){
		on_not_finished();
	}
	$("body").addClass("wait");
	$("#tryForm").show();
	$("#triesDiv").show();
	$.ajax({
		url: '/newGame',
		method: 'post',
		success: on_new_game_success,
		error: on_new_game_error,
		data: {
			sessionId: sessionId
		},
	}).done(function(){
		$("body").removeClass("wait");
	});
	return false;
}

function on_new_game_success(data){
	$("#display").empty();
	$("#rankScroll").hide();
	$("#historyScroll").hide();
	$("#game").show();
	$("#display").show();
	$("#backToGame").hide();
	$("#backToRank").hide();
	$("#display").css("color","green");
	
	 game = new Game();
     var tries_view = new GameResultsView("#tries-template", "#triesDiv");
     game.add_target(tries_view);
     game.on_reset_game();
     idGame = data.id_game;
}

function on_new_game_error(data){
	$("#display").css("color","red");
     $("#display").text(data.responseJSON.description); 
}

/*Registrazione giocatore*/
function on_registration_success(data) {
	$("#suggestionReg").hide();
	$("#display").show();
	on_go_login();
}

function on_registration_error(data) {
	$("#display").text(data.responseJSON.description);
	$("#display").css("color","red");
	$("#suggestionReg").show();
	$("#display").show();
}

function on_registrate() {
	$("body").addClass("wait");
	$.ajax({
		url: '/signup',
		method: 'post',
		success: on_registration_success,
		error: on_registration_error,
		data: {
			id: $("#idRegNick").val(),
			password: $("#idRegPass").val(),
			repeatPassword: $("#idRepeatRegPass").val(),
			email: $("#idRegEmail").val(),
		},
	}).done(function(){
		$("body").removeClass("wait");
	});
	return false;
}

/*Logout*/
function logout() {
	  var cookie_date = new Date ( );  // data corrente
	  /* setto la data di scadenza del cookie ad una precedente a quella attuale 
	  in modo che il browser lo consideri come scaduto e lo ignori */
	  cookie_date.setTime ( cookie_date.getTime() - 1 ); 
	  document.cookie = "session_id=; expires=" + cookie_date.toGMTString();
	  if(game!=null) on_not_finished();
	  location.reload(); // refresh pagina
	  $("#logout").hide();
	}

/*Gestione tentativi*/
function on_try_success(data) {
	$("#display").empty();
	game.on_result_received(data.sequenceNumber, data.sequence, data.result);
	var x=data.result;
	if(x == "++++") {
		$("#display").text("Win!");
		$("#tryForm").hide();
		rateNav(this.sessionId)
	}
}

function on_try_error(data) {
	$("#display").text(data.responseJSON.description);
	$("#display").css("color","red");
	$("#display").show();
	if(data.responseJSON.description=="You have no more tries") rateNav(this.sessionId);
}


function on_try() {
	$("body").addClass("wait");
	var userSequence = $("#sequenceTry").val();
		$.ajax({
			url: '/try',
			method: 'post',
			success: on_try_success,
			error: on_try_error,
			data: {
				sequence: userSequence,
				idGame: game.getIdGame,
			},
		}).done(function(){
			$("body").removeClass("wait");
		});
		return false;
	}

/*Storia partite di un giocatore*/
function on_player_history(user){
	$("body").addClass("wait");
	$.ajax({
		url: '/history',
		method: 'get',
		success: on_history_success,
		data: {
			username: user,
		},
	}).done(function(){
		$("body").removeClass("wait");
	});	
}

function on_history_success (data){
	$("#backToRank").show();
	$("#backToGame").hide();
	$("#display").hide();
	$("#display").empty();
	$("#rankScroll").hide();
	var template = $('#history_template').html();
	var rendered = Mustache.render(template, data);
	$("#historyDiv").html(rendered);
	$("#historyScroll").show();
}

/*Classifica generale*/
function on_rank(){
	$("body").addClass("wait");
	$.ajax({
		url: '/ranking',
		method: 'get',
		success: on_rank_success,
	}).done(function(){
		$("body").removeClass("wait");
	});	
}

function on_rank_success (data){
	if(game!=null){
		$("#backToGame").show();
		}
	
	$("#display").hide();
	$("#display").empty();
	$("#historyScroll").hide();
	$("#game").hide();
	$("#backToRank").hide();
	var template = $('#global_template').html();
	var rendered = Mustache.render(template, data);
	$("#rankDiv").html(rendered);
	$("#rankScroll").show();
}

/*controllo stato del gioco*/
function check_game_status(){
	$("body").addClass("wait");
	$.ajax({
		url: '/checkStatus',
		method: 'get',
		success: on_check_game_status_success,
		data: {
			idGame: game.getIdGame,
		},
	}).done(function(){
		$("body").removeClass("wait");
	});	
}


function on_check_game_status_success(data){
	if (data.gameStatus=="playing"){
		$("#tryForm").show();
	}
	else{
		$("#tryForm").hide();
	}
}

/*pulsanti back*/
function on_back_to_game() {
	game.notify_target;
	$("#display").empty();
	$("#rankScroll").hide();
	$("#game").show();
	$("#display").show();
	$("#backToGame").hide();
	
	if (game!=null){
		check_game_status();
	}
}

function on_back_to_rank() {
	$("#display").empty();
	$("#historyScroll").hide();
	$("#rankScroll").show();
	$("#display").show();
	$("#backToRank").hide();
	if(game!=null){
		$("#backToGame").show();
	}
}

/*Visualizzazione form registrazione*/
function on_go_register(){
	$("#display").hide();
	$("#loginForm").hide();
	$("#labelreg").hide();
	$("#registerForm").show(600);
	$("#labellog").show();
}

/*Visualizzazione form login*/
function on_go_login(){
	$("#registerForm").hide();
	$("#loginForm").show(600);
	$("#labelreg").show();
	$("#labellog").hide();
	$('#suggestionReg').hide();
	$("#display").hide();
}

//Punteggio nella navbar
function rateNav () {
	$.ajax({
		url: '/rateNav',
		method: 'post',
		success: on_rate_nav_success,
		data: {
			sessionId: sessionId,
		},
	});
}

function on_rate_nav_success(data) {
	$("#score").empty();
	$("#score").append("Welcome " + data.username + ", Score: <em style=\"color:black\"> " + data.rate + "</em>");
}

