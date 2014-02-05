<?php
function generateRandomStat($tabify) {
	$requestCount = rand(5, 50);
	$responseCount = rand(5, $requestCount);
	$faultCount = $requestCount - $responseCount;
	
	$minResponse = rand(200, 700);
	$maxResponse = rand($minResponse, 700);
	$avgResponse = rand($minResponse, $maxResponse);
	echo $tabify."<stats>\n";
	echo $tabify."\t<requestCount>".$requestCount."</requestCount>\n";
	echo $tabify."\t<responseCount>".$responseCount."</responseCount>\n";
	echo $tabify."\t<faultCount>".$faultCount."</faultCount>\n";
	echo $tabify."\t<averageResponseTime>".$avgResponse."</averageResponseTime>\n";
	echo $tabify."\t<maximumResponseTime>".$maxResponse."</maximumResponseTime>\n";
	echo $tabify."\t<minimumResponseTime>".$minResponse."</minimumResponseTime>\n";
	echo $tabify."</stats>\n";
}
?>

<services>
	<service name="Service 01">
<?php generateRandomStat("\t\t"); ?>		
		<operations>
			<operation name="getCustomerID">
<?php generateRandomStat("\t\t\t\t"); ?>
			</operation>
			<operation name="canCustomerRate">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="resetRating">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="updateRating">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="setRating">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
		</operations>
	</service>
	<service name="Service 02">
<?php generateRandomStat("\t\t"); ?>		
		<operations>
			<operation name="getCustomerID">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="canCustomerRate">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="resetRating">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="updateRating">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="setRating">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
		</operations>
	</service>	        
	<service name="Service 03">
<?php generateRandomStat("\t\t"); ?>		
		<operations>
			<operation name="findDNSServers">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="resetDNSServer">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="startDNSServer">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
			<operation name="stopDNSServer">
<?php generateRandomStat("\t\t\t\t"); ?>		
			</operation>
		</operations>
	</service>	
</services>
