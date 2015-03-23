function updateWidgetList(data,dataTable) {
    console.log("updateCalled",data);

    // d3.select("#widgets").selectAll('div.col-lg-3').data(data.widgets).enter().append('div').attr('class', 'col-lg-3 views').html(
    //         function (d) {
    //             var panel;
    //             var name;
    //             var text;
    //             var image;
    //             switch (d.config.chartType) {
    //                 case "bar":
    //                     panel = "panel-primary";
    //                     name = "Bar Chart";
    //                     image = "fa-bar-chart"
    //                     break;
    //                 case "line":
    //                     panel = "panel-green";
    //                     name = "Line Chart";

    //                     image = "fa-line-chart"
    //                     break;
    //                 case "area":
    //                     panel = "panel-red";
    //                     name = "Area Chart";
    //                     image = "fa-area-chart"
    //                     break;
    //             }
    //             name = "";
    //             text = "<big>" + d.title.substr(0, d.title.lastIndexOf('By') - 1) + "</big><br>" + "<small>Vs</small><br/><big>" + d.title.substr(d.title.lastIndexOf('By') + 2) + "</big>";
    //             string = '<div class="panel ' + panel + '"><div class="panel-heading"><div class="row"><div class="col-xs-3"><i class="fa ' + image + ' fa-5x"></i></div><div class="col-xs-9 text-right"><div>' + text + '</div><div>' + name + '</div></div></div></div><a href="#"><div class="panel-footer"><span class="pull-left">'+ d.title+'</span><span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span><div class="clearfix"></div></div></a></div>'
    //             return string
    //         });
                


                function modelShow() {

                    $("#hiddenForm form").get(0).reset();
                    jQuery("#hiddenForm form .attr").hide();
                    jQuery("#chartDiv").empty();
                    jQuery("#data").empty();
                    dTable = d3.select("#data").append('table').attr('class', 'table table-bordered');
                    thead = dTable.append('thead');
                    thead.selectAll('td').data(dataTable.metadata.names).enter().append('td').html(function (d) {
                       // console.log(d);
                        return d;
                    })

                    jQuery("#modalSave").hide();

                    for (i = 0; i < dataTable.data.length; i++) {
                        tr = dTable.append('tr');
                        tr.selectAll('td').data(dataTable.data[i]).enter().append('td').html(function (d) {
                         //   console.log(d);
                            return d;
                        })

                    }

                }

                d3.selectAll(".views").on('click', function (d, i) {

                    $("#myModalLabel").html(d.title);
                    modelShow();
                    width = 500;
                    height = 270;

                    var config = {
                        "title": d.title,
                        "yAxis": d.config.yAxis,
                        "xAxis": d.config.xAxis,
                        "width": width,
                        "height": height,
                        "chartType": d.config.chartType
                    }

                    switch (d.config.chartType) {
                        case 'line':
                            config.yAxis = [d.config.yAxis];

                            break;
                        case 'bar':
                            break;
                        case 'area':
                            break;
                    }

                    chart = igviz.setUp("#chartDiv", config, dataTable);
                    setTimeout(function () {
                        chart.plot(dataTable.data)
                    }, 200)
//                chart.plot(dataTable.data)


                    $('#myModal').modal('show')


                })

                $(".addNew").click(function () {
                    $("#myModalLabel").html("Add New Widget");
                    modelShow();
                    $('#myModal').modal('show')
                });


                d3.selectAll(".var select").selectAll('option').data(dataTable.metadata.names).enter().append('option').attr('val', function (d) {
                    return d
                }).html(function (d) {
                    return d
                });
                // dataTable.metadata.names

                Dform = jQuery("#hiddenForm form .attr");
                Dform.hide();




                jQuery('#chartType').on('change', function (e) {
                    Dform.hide();
                    className = jQuery(this).children(":selected").val()
                    jQuery("." + className).show();

                    jQuery('#preview').on('click', function (e) {
                        chartConfigObj = {};
                        jQuery('.' + className).each(function (i) {


                            dObj = d3.select(this).select(".form-control");


//                          fObj=jQuery(this).children(".form-control");

//                        console.log(fObj.get);
                            if (jQuery(this).hasClass('var')) {

                                if (dObj[0][0].multiple) {

                                    chartConfigObj.yAxis = [];
                                    optList = dObj[0][0].options
                                    for (i = 0; i < optList.length; i++) {
                                        if (optList[i].selected) {
                                            chartConfigObj.yAxis.push(i);
                                        }
                                    }
                                } else

                                    chartConfigObj[dObj.attr('name')] = dObj[0][0].selectedIndex;
                            }
                            else {
                                //  console.log("Not value",dObj);
                                chartConfigObj[dObj.attr('name')] = dObj[0][0].value;
                            }


                        });

                        switch (className) {
                            case "stackedArea":
                                chartConfigObj.chartType = "area";
                                break;
                            case "groupedBar":
                                chartConfigObj.chartType = "bar";
                                break;
                            case "multiArea":
                                chartConfigObj.chartType = "area";
                                break;
                            case "stackedBar":
                                chartConfigObj.chartType = "bar";
                                chartConfigObj.format = "stacked";
                                break;
                            default:
                                chartConfigObj.chartType = className;
                        }


                        console.log(chartConfigObj)


                        chartConfigObj.width = 400;
                        chartConfigObj.height = 300;

                        myChart = igviz.setUp("#chartDiv", chartConfigObj, dataTable);
                        myChart.plot(dataTable.data);


                        $("#modalSave").show();

                    });

                    d3.select('#modalSave').on('click', function () {
                        if (d3.select("#myModalLabel").text() === "Add New Widget") {
                            widgetObj = {
                                id: "343dfadfadf",
                                title: chartConfigObj.title,
                                config: JSON.parse(JSON.stringify(chartConfigObj))
                            };
                            console.log(widgetObj); 

                            data.widgets.push(widgetObj);
                            // updateWidgetList(data,dataTable);
                            addWidgetToDataView(widgetObj,data.id);

                        }
                        else {
                            //update existing widgets

                            console.log('updated');
                        }
                        

                        $('#myModal').modal('hide');



                    });
                    //end of save click







                });

                

                d3.select('#modalSave').on('click', function () {
                    $('#myModal').modal('hide');

                })
            }

