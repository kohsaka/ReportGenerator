package hit.repgen.renderer.file;

import hit.repgen.renderer.RenderingTemplate;

public class FileReportTemplate extends RenderingTemplate{
	
	private String templateContents;
	
	public FileReportTemplate(){
		this.templateContents = null;
	}

	/**
	 * @return templateContents
	 */
	public String getTemplateContents() {
		return templateContents;
	}

	/**
	 * @param templateContents �Z�b�g���� templateContents
	 */
	public void setTemplateContents(String templateContents) {
		this.templateContents = templateContents;
	}

	

}
