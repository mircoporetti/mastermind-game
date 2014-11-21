function GameResultsView(template, target) {
  this.template = template;
  this.target = target;
}

GameResultsView.prototype.notify = function(game) {
  var template = $(this.template).html();
  var rendered = Mustache.render(template, {
    tries: game.tries,
  });
  $(this.target).html(rendered);
}