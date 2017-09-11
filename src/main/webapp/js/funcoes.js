function getWidhtBrowserResolution(){
	var resolution;
	resolution = windows.screen.width;
	return new String(resolution);
}

function getHeightBrowserResolution(){
	var resolution;
	resolution = windows.screen.height;
	return new String(resolution);
}

function dimChartX(){
	return screen.availWidth/2;
}

function dimChartY(){
	return screen.availHeight/2;
}