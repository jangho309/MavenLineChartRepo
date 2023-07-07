<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="icon" href="data:,">
	<script src="js/highcharts.js" type="text/javascript"></script>
	<script src="js/accessibility.js" type="text/javascript"></script>
	<script type="text/javascript">
		document.addEventListener("DOMContentLoaded", function(){
			
// 			console.log("시간값 : " + document.getElementById("startDate").value + ", len : " + document.getElementById("startDate").value.length);
			if("" == document.getElementById("startDate").value && "" == document.getElementById("endDate").value){
				var getStartValue = '${getStartValue}'.substr(0, '${getStartValue}'.length - 7);
				var getEndValue = '${getEndValue}'.substr(0, '${getEndValue}'.length - 7);
				document.getElementById("startDate").value = getStartValue.split(' ')[0];
				document.getElementById("startHms").value = getStartValue.split(' ')[1];
				document.getElementById("endDate").value = getEndValue.split(' ')[0];
				document.getElementById("endHms").value = getEndValue.split(' ')[1];
				drawChart('${getData}');
			}
			
// 			var chartData = makeData();
// 			var timeData = ['2022-08-24 16:04:54.000', '2022-08-24 15:59:54.000', '2022-08-24 15:54:54.000', '2022-08-24 15:49:54.000', '2022-08-24 15:44:54.000', '2022-08-24 15:39:55.000', '2022-08-24 15:34:55.000', '2022-08-24 15:29:55.000', '2022-08-24 15:24:55.000', '2022-08-24 15:19:55.000'];
// 			var data ='[{"name":"z_mempct","data":[61.985245, 61.89274, 61.44039, 61.36844, 61.49242, 61.84856, 62.036526, 61.787876, 61.470352, 61.399574]},
// 							   {"name":"z_fsavg","data":[20.164306640625, 20.163982391357422, 20.16368293762207, 20.163379669189453, 20.16307830810547, 20.162765502929688, 20.162477493286133, 20.162174224853516, 20.161849975585938, 20.16153335571289]},
// 							   {"name":"z_cpupct","data":[9.535376, 9.543722, 9.786568, 9.650726, 9.604276, 9.320127, 9.184128, 9.463326, 9.414691, 9.590064]}]';
			
			
			document.getElementById("search").onmousedown = function(){
				var startValue = document.getElementById("startDate").value + " " + document.getElementById("startHms").value;
				var endValue = document.getElementById("endDate").value + " " + document.getElementById("endHms").value;

				reloadChart(startValue, endValue);
// 				console.log("시작일자 : " + startValue + ", 종료일자 : " + endValue);

			};
		});
		
		function reloadChart(startDate, endDate){
			var rtnData;
			// 비동기 객체 생성
			let request = new XMLHttpRequest();
			
			// 1 번째 파라미터 : requestMethod, 2 번째 파라미터 : 보낼 servlet url, 3 번째 파라미타 : true(비동기) / false(동기)
			let sendData = "startDate=" + startDate + "&endDate=" + endDate;
			let url = '/ChartProject/drawChart';
			request.open('POST', url, true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send(sendData);
			
			request.onreadystatechange = function(){
// 				console.log("readyState : " + request.readyState + ", status : " + request.status);
				// readyState : 0 -> XMLHttpRequest 객체가 생성됨
				// readyState : 1 -> open() 메소드가 성공적으로 실행됨.
				// readyState : 2 -> 모든 요청에 대한 응답이 도착함.
				// readyState : 3 -> 요청한 데이터를 처리 중임.
				// readyState : 4 -> 요청한 데이터의 처리가 완료되어 응답할 준비가 완료됨.
				// status : 200 -> 서버에 문서가 존재함
				if(request.readyState == 4 && request.status == 200){
					rtnData = request.response;
// 					console.log("rtnData : " + rtnData);
					drawChart(rtnData);
				} 
			};
		}
		
		function drawChart(data){
			var dataObj;
			if("string" == typeof(data)){
				dataObj = JSON.parse(data);
			} else {
				dataObj = data;
			}
			 
// 			console.log("data : " + JSON.stringify(data));
			let chartData = chartDataParsing(dataObj);
			Highcharts.chart('container', {
				title: {
					text: 'SMS 장비 성능 차트'
				},
				subtitle: {
					text: 'CPU, Memory, Disk 평균 사용률'
				},
				xAxis : {
					tickInterval : chartData.interval,
					type : 'datetime',
					title : {
						text : 'Collect Time'
					},
					labels : {
						// time 데이터를 '월-일 시:분' 형태로 변형
						formatter : function(){
							let timeValue = this.value;
							timeValue = timeValue.substr(5, 11);
							return timeValue;
						}
					},
					categories : chartData.timeData
				},
				yAxis: {
					min : 0,
			        max : 100,
					title: {
						text: 'uesd(%)'
				    }
				},
				series: chartData.seriesData
			});
		}
		
		function chartDataParsing(data){
			var rtnData = {};
// 			var categoriesData = [];
			var seriesData = [];
			var timeData = [];
			var cpuMap = {'data' : []};
			var memMap = {'data' : []};
			var diskMap = {'data' : []};
// 			console.log("first time data : " + data[0]["z_dt"] + ", last time data : " + data[data.length - 1]["z_dt"]);
			if(data.length > 1){
				var firstDate = new Date(data[0]["z_dt"]);
				var lastDate = new Date(data[data.length - 1]["z_dt"]);
				
				var diff = lastDate.getTime() - firstDate.getTime();
				var timediff = diff / (1000 * 60 * 60 * 24);
				var interval = 0;
				if(timediff > 0.5){
					interval = 72;
				} else if(timediff > 0.25 && timediff <= 0.5){
					interval = 15;
				} else if(timediff > 0.1 && timediff <= 0.25){
					interval = 10;
				} else {
					interval = 1;
				}
				rtnData['interval'] = interval;
// 				console.log("일수 차이 : " + timediff);
			}
			
			for(let i = 0; i < data.length; i++){
				for(key in data[i]){
					if("z_dt" === key){
						timeData.push(data[i][key]);
					}
					
					if("z_cpupct" === key){
						cpuMap['name'] = 'CPU';
						cpuMap['color'] = '#FFB74D';
						cpuMap['data'].push(data[i][key]);
					}
					
					if("z_mempct" === key){
						memMap['name'] = 'Memory';
						memMap['color'] = '#00FF00';
						memMap['data'].push(data[i][key]);
					}
					
					if("z_fsavg" === key){
						diskMap['name'] = 'Disk';
						diskMap['color'] = '#7C4DFF';
						diskMap['data'].push(data[i][key]);
					}
				}
			}
			seriesData.push(cpuMap);
			seriesData.push(memMap);
			seriesData.push(diskMap);
// 			console.log("cpuMap : " + JSON.stringify(cpuMap));
// 			console.log("memMap : " + JSON.stringify(memMap));
// 			console.log("diskMap : " + JSON.stringify(diskMap));
// 			console.log("seriesData : " + JSON.stringify(seriesData));
// 			console.log("categoriesData : " + JSON.stringify(categoriesData));
// 			rtnData['categoriesData'] = categoriesData;
			rtnData['seriesData'] = seriesData;
			rtnData['timeData'] = timeData;
			console.log("rtnData : " + JSON.stringify(rtnData));
			return rtnData;
		}
		
// 		function makeData(){
// 			var seriesData = [];
		   
// 			for(let i = 0; i <= 4; i++){
// 				let mapData = {};
// 				mapData['name'] = "컬럼" + (i + 1);
// 				mapData['data'] = makeArrayData();
// 				seriesData.push(mapData);
// 			}
// // 			console.log("data : " + JSON.stringify(seriesData));
// 			return seriesData;
// 		}

// 		function makeArrayData(){
// 			var rArray = [];
// 			for(let i = 1; i <= 10000; i++){
// 			    const rand1_300 = Math.floor(Math.random() * 300) + 1;
// 			    rArray.push(rand1_300);
// 			}
// 			return rArray;
// 		}
	</script>
	<title>HighChart For SMS</title>
</head>
<body>
	<table class="chart_tb">
		<colgroup>
			<col style="width : 100px">
			<col style="width : 500px">
			<col style="width : 50px">
		</colgroup>
		<tbody>
			<tr>
				<td><h3>검색기간</h3></td>
				<td><input type="date" id="startDate"> <input type="time" id="startHms"> ~ <input type="date" id="endDate"> <input type="time" id="endHms"></td>
				<td><input type="button" id="search" value="검색"></td>
			</tr>
		</tbody>
	</table>
	<div id="container"></div>
</body>
</html>