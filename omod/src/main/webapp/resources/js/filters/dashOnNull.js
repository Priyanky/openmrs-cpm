define(['./index'], function(filters) {

  filters.filter('dashOnNull', function() {
    return function(input) {
      if (typeof input === 'undefined' || input === '' || input === null) {
          return "\u2013";
      } else {
          return input;
      }
    };
  });
});