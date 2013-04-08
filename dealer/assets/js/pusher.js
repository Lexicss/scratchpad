var Pusher = function($, C) {
  C.INITIALIZED = false;
  
  C.init = function() {
    C.INITIALIZED = true;
  };
  
  C.receive = function(content) {
    if (C.INITIALIZED) {
      var append = "";
      append += "<li>";
      append += content['message'];
      
      if (Object.keys(content).length > 1) {
        append += "<ul>";
        
        for (var k in content) {
          if (k == "message") continue;
          
          append += "<li>" + k + ": " + content[k] + "</li>";
        }
        
        append += "</ul>";
      }

      append += "</li>";

      $('#messages').append(append);

      return true;
    } else {
      return false;
    }
  };
  
  return C;
}($, Pusher || {});
