<?php
function generateRandomStat($tabify) {
	$requestCount = rand(5, 50);
	$responseCount = rand(5, $requestCount);
	$faultCount = $requestCount - $responseCount;

	echo $tabify."\t<stats name=\"Requests\" count=\"".$requestCount."\" />\n";
	echo $tabify."\t<stats name=\"Faults\" count=\"".$faultCount."\"/>\n";
}
?>

<level0 name="https://10.100.1.119:8243/esb">
        <?php generateRandomStat("\t\t"); ?>
		<level1 name="endpoint 01">
            <?php generateRandomStat("\t\t\t"); ?>
            <level2 name="operation 01">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 02">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 03">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
		</level1>
		<level1 name="endpoint 02">
            <?php generateRandomStat("\t\t\t"); ?>
            <level2 name="operation 01">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 02">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 03">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
		</level1>
		<level1 name="endpoint 03">
            <?php generateRandomStat("\t\t\t"); ?>
            <level2 name="operation 01">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 02">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 03">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
		</level1>
		<level1 name="endpoint 04">
            <?php generateRandomStat("\t\t\t"); ?>
            <level2 name="operation 01">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 02">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 03">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
		</level1>
		<level1 name="endpoint 05">
            <?php generateRandomStat("\t\t\t"); ?>
            <level2 name="operation 01">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 02">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
            <level2 name="operation 03">
                <?php generateRandomStat("\t\t\t\t"); ?>
		    </level2>
		</level1>
</level0>
