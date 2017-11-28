$(function() {
    $( "#newPlayerList" ).hide();
    $( "#newPlayerButton" ).click(function() {
        $( "#newPlayerList" ).show();
        $( "#newPlayerList" ).dialog({
            buttons: {
                "Add": function() {
                    $(".newPlayerDialog .ui-dialog-buttonpane button:contains('Add')").button("disable");
                    $(".newPlayerDialog .ui-dialog-buttonpane button:contains('Add')").text("Adding Player...");
                    var xhttp = new XMLHttpRequest();
                    xhttp.onreadystatechange = function() {
                        if (this.readyState == 4 && this.status == 200) {
                            location.reload();
                        }
                    };
                    xhttp.open("GET", "/addPlayer?" + $("#newPlayerList").find("select").serialize(), true);
                    xhttp.send();
                }
            },
            dialogClass: 'newPlayerDialog'
        });
    });
});

function removePlayer(player) {
    document.getElementById(player).disabled = true;
    document.getElementById(player).innerHTML = "Removing Player...";
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            location.reload();
        }
    };
    xhttp.open("GET", "/removePlayer?playerName=" + player, true);
    xhttp.send();
}