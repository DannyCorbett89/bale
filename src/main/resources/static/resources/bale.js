$(function() {
    $( "#newPlayerList" ).hide();
    $( "#newPlayerButton" ).click(function() {
        $( "#newPlayerList" ).show();
        $( "#newPlayerList" ).dialog({
            buttons: {
                "Add": function() {
                    $(".newPlayerDialog .ui-dialog-buttonpane button:contains('Add')").button("disable");
                    $(".newPlayerDialog .ui-dialog-buttonpane button:contains('Add')").text("Adding Player...");
                    $.ajax({
                        url: "/addPlayer",
                        timeout: 30000,
                        type: "POST",
                        data: $("#newPlayerList").find("select").serialize(),
                        dataType: 'json',
                        error: function(XMLHttpRequest, textStatus, errorThrown)  {
                            alert("An error has occurred making the request: " + errorThrown)
                        },
                        success: function(data){
                            location.reload();
                        }
                    })
                }
            },
            dialogClass: 'newPlayerDialog'
        });
    });
});
function removePlayer(player) {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            location.reload();
        }
    };
    xhttp.open("GET", "/removePlayer?playerName=" + player, true);
    xhttp.send();
    document.getElementById(player).disabled = true;
    document.getElementById(player).innerHTML = "Removing Player...";
}