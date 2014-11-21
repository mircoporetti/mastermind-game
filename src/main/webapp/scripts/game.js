function Game() {
  this.idGame;
  this.tries = [];
  this.target;
}

Game.prototype.on_result_received = function(idTry, sequence, result) {
  this.tries.push({SequenceNumber: idTry, InputNumbers: sequence, Result: result});

  this.notify_target();
}

Game.prototype.on_reset_game = function() {
	  this.tries = [];
	  
	  this.notify_target();
	}

Game.prototype.add_target = function(o) {
	this.target = o;
}


Game.prototype.notify_target = function() {
   this.target.notify(this);
}

Game.prototype.setIdGame = function(o){
	this.idGame = o;
}

Game.prototype.getIdGame = function(o){
	return this.idGame;
}

Game.prototype.getUsername = function(o){
	return this.username;
}