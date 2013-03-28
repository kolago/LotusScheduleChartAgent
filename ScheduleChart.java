import java.awt.Color;
import java.awt.Font;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.local.DateTime;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.general.Series;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.TextAnchor;


public class ScheduleChart extends ApplicationFrame {
	private static final long serialVersionUID = -7234427603759192903L;

	public ScheduleChart(String title, List projects, Map projectToDocumentsMap) {
        super(title);

        final IntervalCategoryDataset dataset = createDataset(projects, projectToDocumentsMap);
        final JFreeChart chart = createChart(dataset);
        /* Font for displaying Chinese correctly. */
        Font chineseFont = new Font("SimSun", Font.PLAIN, 10);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        chart.getLegend().setItemFont(chineseFont);
        CategoryItemRenderer renderer = plot.getRenderer();
        /* For displaying label on gantt bar. */
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelPaint(Color.BLACK);
        renderer.setBaseItemLabelFont(chineseFont);
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
        ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER));
        renderer.setBaseItemLabelGenerator( new CategoryItemLabelGenerator(){

            public String generateRowLabel(CategoryDataset dataset, int row) {
                return "";
            }

            public String generateColumnLabel(CategoryDataset dataset, int column) {
                return "";
            }

            public String generateLabel(CategoryDataset dataset, int row, int column) {
            	Series task = (Series) (dataset.getRowKeys()).get(row);
            	return task.getDescription();
            }


        });
        // add the chart to a panel...
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
	}

	private IntervalCategoryDataset createDataset(List projects, Map projectToDocumentsMap) {
		final TaskSeriesCollection collection = new TaskSeriesCollection();
		for (int i=0; i<projects.size(); i++) {
			String projectName = (String) projects.get(i);
			TaskSeries taskSeries = new TaskSeries(projectName);
			taskSeries.setDescription(projectName);
			List documents = (List) projectToDocumentsMap.get(projectName);
			for (int j=0; j<documents.size(); j++) {
				Document doc = (Document) documents.get(j);
				String userNameFullString = null;
				try {
					userNameFullString = (String) doc.getItemValue("PersonArrangementForm_Name").get(0);
				} catch (NotesException e1) {
					e1.printStackTrace();
				}
				String username = parse(userNameFullString);
				
				DateTime startDominalDate = null;
				DateTime endDominalDate = null;
				
				try {
					startDominalDate = (DateTime) doc.getItemValue("PersonArrangementForm_StartDate").get(0);
					endDominalDate = (DateTime) doc.getItemValue("PersonArrangementForm_EndDate").get(0);
				} catch (NotesException e) {
					e.printStackTrace();
				}
				
				Date startJavaDate = null;
				Date endJavaDate = null;
				try {
					startJavaDate = startDominalDate.toJavaDate();
					endJavaDate = endDominalDate.toJavaDate();
				} catch (NotesException e) {
					e.printStackTrace();
				}
				
				Task newTask = new Task(username, new SimpleTimePeriod(startJavaDate, endJavaDate));
				taskSeries.add(newTask);
			}
			collection.add(taskSeries);
		}
		return collection;
	}

    private String parse(String userNameFullString) {
    	String[] splittedStrArray = userNameFullString.split("/OU");
		String patternString = "CN=(.*)";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(splittedStrArray[0]);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
    
	private JFreeChart createChart(final IntervalCategoryDataset dataset) {
		JFreeChart chart = ChartFactory.createGanttChart(
		"Arrangement Summary", 
		"Task", 
		"Time", 
		dataset, 
		true, 
		true, 
		false 
		);
//        chart.getCategoryPlot().getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
        return chart;    
    }
}
