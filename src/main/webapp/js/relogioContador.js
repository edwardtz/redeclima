var Timer;
				
var TotalSeconds;

/* Cria um relogio countdown 
 * TimerID identificador do componente html onde se renderizara o relogio
 * Time    cantidade em segundos donde inicia o relogio
 * */
function CreateTimer(TimerID, Time) {
	Timer = document.getElementById(TimerID);
	TotalSeconds = Time;
	UpdateTimer();
	window.setTimeout("Tick()", 1000);
}

function Tick() {
	if (TotalSeconds <= 0) {
		TotalSeconds = 600; //10 minutos
		updateMap(); //TODO chamada para o remoteCommand da pagina MonitoramentoBaciaMap, tem que ser desacoplado daquim
	}
	TotalSeconds -= 1;
	UpdateTimer()
	window.setTimeout("Tick()", 1000);
}

function UpdateTimer() {
	
	var Seconds = TotalSeconds;

	var Days = Math.floor(Seconds / 86400);
	Seconds -= Days * 86400;

	var Hours = Math.floor(Seconds / 3600);
	Seconds -= Hours * (3600);

	var Minutes = Math.floor(Seconds / 60);
	Seconds -= Minutes * (60);

	var TimeStr = LeadingZero(Minutes) + ":" + LeadingZero(Seconds);
	Timer.innerHTML = "Atualização em " + TimeStr;
	
}

function LeadingZero(Time) {
	return (Time < 10) ? "0" + Time : + Time;
}