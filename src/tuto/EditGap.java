package tuto;

public class EditGap extends AbstractTutoStep {

	public double newParameterValue;
	public boolean hasValueBeenChanged = false;
	
	public EditGap(double value){
		newParameterValue = value;
	}
	
	@Override
	public void stepInitialization() {
		getSelectionPanel().jb_process_extraction.setEnabled(true);
		getSelectionPanel().jtf_gap_score.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jb_process_extraction.setEnabled(false);
		getSelectionPanel().jtf_gap_score.setEnabled(false);
	}

	@Override
	public String description() {
		return "The next parameter that can be modified is called the <i>gap cost</i>. It corresponds to the cost of having one empty line between two annotations in an alignment. <br>"
				+ "For example, the following alignment contains one gap:<br>"
				+ "<table  align=\"center\" style=\"margin: 0px auto;\">"
  +"<tr>"
  + "<th></th>"
    +"<th>First array</th>"
    +"<th>Second array</th>"
  +"</tr>"
  +"<tr>"
  + "<td></td>"
+"    <td align=\"center\" valign=\"middle\">A</td>"
    +"<td align=\"center\" valign=\"middle\">A</td> "
  +"</tr>"
  +"<tr>"
  + "<td>empty line ---></td>"
+"    <td></td>"
    +"<td></td>" 
  +"</tr>"
  +"<tr>"
  +"<td></td>" 
+"    <td align=\"center\" valign=\"middle\">B</td>"
    +"<td align=\"center\" valign=\"middle\">B</td>" 
  +"</tr>"
+"</table>"
			+ "The longer the gap, the higher the penalty on the alignment score.<br>"
				+ "As a result, decreasing the gap cost may increase the size of the obtained alignments.<br>"
				+ "Currently, this cost is very high, we will decrease it to observe bigger alignments.";
	}

	@Override
	public String instructions() {
		return "- Go the \"Data selection\" tab<br>"
				+ "- Change the value of the gap cost to " + newParameterValue + "<br>"
				+ "- Start the extraction";
	}

	@Override
	public String resultsComment() {
		return "You can see that by decreasing the gap cost, some alignments containing empty lines were obtained.<br><br>"
				+ "Another interesting observation is that decreasing the gap cost also allows alignments with the following configuration:"
				+ "<table  align=\"center\" style=\"margin: 0px auto;\">"
  +"<tr>"
    +"<th>First array</th>"
    +"<th>Second array</th>"
  +"</tr>"
  +"<tr>"
+"    <td align=\"center\" valign=\"middle\">A&nbsp C</td>"
    +"<td align=\"center\" valign=\"middle\">A</td> "
  +"</tr>"
  +"<tr>"
+"    <td align=\"center\" valign=\"middle\"></td>"
    +"<td align=\"center\" valign=\"middle\"> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp C</td>" 
  +"</tr>"
+"</table>"
				+ "We call this type of configuration a <i>desynchronization</i>. It may lead to alignments with different shapes in both annotation arrays.";
	}

}
