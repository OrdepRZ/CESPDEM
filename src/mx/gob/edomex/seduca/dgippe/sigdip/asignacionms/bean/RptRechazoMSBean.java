package mx.gob.edomex.seduca.dgippe.sigdip.asignacionms.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.dto.AspiranteDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.dto.CifraDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacionms.dao.AspiranteMSDAO;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacionms.impl.AspiranteMSDaoImpl;
import mx.gob.edomex.seduca.dgippe.sigdip.bean.login.LoginBean;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.dao.CatalogoDAO;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.dto.CatalogoDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.impl.CatalogoDaoImpl;
import mx.gob.edomex.seduca.dgippe.sigdip.comun.dto.CampoRptDTO;

@ManagedBean
@ViewScoped
public class RptRechazoMSBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String idConvocatoria;
	private String idRechazo;
	private String idReporte;
	private String idSubsistema;
	private String idFuncion;
	private String idAsignatura;
	private Date fecInicial;
	private Date fecFinal;
	private boolean bSubsistema;
	private List<CatalogoDTO> lstConvocatoria;
	private List<CatalogoDTO> lstRechazo;
	private List<CatalogoDTO> lstFuncion;
	private List<CatalogoDTO> lstSubsistema;
	private List<CatalogoDTO> lstAsignatura;
	private List<AspiranteDTO> lstAspirante;
	private List<CifraDTO> lstCifra;
	private boolean bndConsulta;
	private boolean bndDetalle;
	private LoginBean loginBean;
	
	public RptRechazoMSBean() {
		idConvocatoria = "0";
		idRechazo = "0";
		idSubsistema = "0";
		idReporte = "0";
		idFuncion = "0";
		idAsignatura = "0";
		bSubsistema = false;

		lstConvocatoria = new ArrayList<CatalogoDTO>();
		lstRechazo = new ArrayList<CatalogoDTO>();
		lstSubsistema = new ArrayList<CatalogoDTO>();
		lstFuncion = new ArrayList<CatalogoDTO>();
		lstAsignatura = new ArrayList<CatalogoDTO>();
		lstAspirante = new ArrayList<AspiranteDTO>();
		lstCifra = new ArrayList<CifraDTO>();

		mostrarConsulta();
		
		try {
			//Cargar lista de Convocatorias.
			CatalogoDAO catalogoDAO = new CatalogoDaoImpl();
			lstConvocatoria = catalogoDAO.getCatalogoItems("ASGMSCONVO");
			lstRechazo = catalogoDAO.getCatalogoItems("ASGMTVRECH");
			lstSubsistema = catalogoDAO.getCatalogoItems("ASGMSSUBSIS");
			lstFuncion = catalogoDAO.getCatalogoItems("ASGMSFUNASP");
			lstAsignatura = catalogoDAO.getCatalogoItems("ASGMSASIGN"); 
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
			loginBean = (LoginBean) session.getAttribute("loginBean");	
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "RptRechazoMSBean() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public String consultar() {
		String resultado = null;
		
		lstAspirante = new ArrayList<AspiranteDTO>();
		lstCifra = new ArrayList<CifraDTO>();
		
		if(idConvocatoria == null || idConvocatoria.trim().equals("") || idConvocatoria.trim().equals("0")) {
			FacesMessage msg = new FacesMessage("Por favor selecciona la Convocatoria.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return resultado;
		}
		
		try {
			
			List<CampoRptDTO> lstFiltro = new ArrayList<CampoRptDTO>();					
			CampoRptDTO idFiltro;

			//ESTATUS 2 - RECHAZADO 
			idFiltro = new CampoRptDTO();
			idFiltro.setClave("STATUS");
			idFiltro.setValor("3");
			lstFiltro.add(idFiltro);
			
			if(idConvocatoria != null && !idConvocatoria.trim().equals("") && !idConvocatoria.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_CONVOCATORIA");
				idFiltro.setValor(idConvocatoria.trim());
				lstFiltro.add(idFiltro);
			}

			if(idRechazo != null && !idRechazo.trim().equals("") && !idRechazo.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("MOTIVO_RECHAZO");
				idFiltro.setValor(idRechazo.trim());
				lstFiltro.add(idFiltro);
			}

			if (loginBean.getUsuario() != null && loginBean.getUsuario().getIdSubsistema() != null 
					&& !loginBean.getUsuario().getIdSubsistema().trim().equals("") && !loginBean.getUsuario().getIdSubsistema().trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_SUBSISTEMA");
				idFiltro.setValor(loginBean.getUsuario().getIdSubsistema());
				lstFiltro.add(idFiltro);				
			} else if(idSubsistema != null && !idSubsistema.trim().equals("") && !idSubsistema.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_SUBSISTEMA");
				idFiltro.setValor(idSubsistema);
				lstFiltro.add(idFiltro);
			}
			
			if(idReporte != null && !idReporte.trim().equals("") && !idReporte.trim().equals("0")) {
				if (idReporte.trim().equals("1")){
					//INGRESO
					idFiltro = new CampoRptDTO();
					idFiltro.setClave("INGRESO");
					idFiltro.setValor(idFuncion.trim());
					lstFiltro.add(idFiltro);
				} else if (idReporte.trim().equals("2")){
					//PROMOCIÓN
					idFiltro = new CampoRptDTO();
					idFiltro.setClave("PROMOCION");
					idFiltro.setValor(idFuncion.trim());
					lstFiltro.add(idFiltro);
					
				}
			}
			
			if(idFuncion != null && !idFuncion.trim().equals("") && !idFuncion.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_FUNCION");
				idFiltro.setValor(idFuncion.trim());
				lstFiltro.add(idFiltro);
			}

			if(idAsignatura != null && !idAsignatura.trim().equals("") && !idAsignatura.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("CVEASIGNATURA");
				idFiltro.setValor(idAsignatura.trim());
				lstFiltro.add(idFiltro);
			}

			if (fecInicial != null) {
				SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
				
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("FECRECHAZO_INICIAL");
				idFiltro.setValor(formatoFecha.format(fecInicial));
				idFiltro.setCadena(true);
				
				lstFiltro.add(idFiltro);
			}

			if (fecFinal != null) {
				SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
				
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("FECRECHAZO_FINAL");
				idFiltro.setValor(formatoFecha.format(fecFinal));
				idFiltro.setCadena(true);
				
				lstFiltro.add(idFiltro);
			}
			
			AspiranteMSDAO aspiranteDAO = new AspiranteMSDaoImpl();
			//lstAspirante = aspiranteDAO.getDetalleAspirantes(lstFiltro);
			lstCifra = aspiranteDAO.getCifrasRechazo(lstFiltro);
			
			if(lstCifra.isEmpty()) {
				FacesMessage msg = new FacesMessage("No se localizarón registros con los filtros seleccionados.");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
				
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "consultar() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return resultado;
	}

	public String consultarDetalle() {
		String resultado = null;
		
		lstAspirante = new ArrayList<AspiranteDTO>();
		//lstCifra = new ArrayList<CifraDTO>();
		
		if(idConvocatoria == null || idConvocatoria.trim().equals("") || idConvocatoria.trim().equals("0")) {
			FacesMessage msg = new FacesMessage("Por favor selecciona la Convocatoria.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return resultado;
		}
		
		try {
			
			List<CampoRptDTO> lstFiltro = new ArrayList<CampoRptDTO>();					
			CampoRptDTO idFiltro;

			//ESTATUS 2 - RECHAZADO 
			idFiltro = new CampoRptDTO();
			idFiltro.setClave("STATUS");
			idFiltro.setValor("3");
			lstFiltro.add(idFiltro);
			
			if(idConvocatoria != null && !idConvocatoria.trim().equals("") && !idConvocatoria.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_CONVOCATORIA");
				idFiltro.setValor(idConvocatoria.trim());
				lstFiltro.add(idFiltro);
			}

			if(idRechazo != null && !idRechazo.trim().equals("") && !idRechazo.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("MOTIVO_RECHAZO");
				idFiltro.setValor(idRechazo.trim());
				lstFiltro.add(idFiltro);
			}

			if (loginBean.getUsuario() != null && loginBean.getUsuario().getIdSubsistema() != null 
					&& !loginBean.getUsuario().getIdSubsistema().trim().equals("") && !loginBean.getUsuario().getIdSubsistema().trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_SUBSISTEMA");
				idFiltro.setValor(loginBean.getUsuario().getIdSubsistema());
				lstFiltro.add(idFiltro);				
			} else if(idSubsistema != null && !idSubsistema.trim().equals("") && !idSubsistema.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_SUBSISTEMA");
				idFiltro.setValor(idSubsistema);
				lstFiltro.add(idFiltro);
			}
			
			if(idReporte != null && !idReporte.trim().equals("") && !idReporte.trim().equals("0")) {
				if (idReporte.trim().equals("1")){
					//INGRESO
					idFiltro = new CampoRptDTO();
					idFiltro.setClave("INGRESO");
					idFiltro.setValor(idFuncion.trim());
					lstFiltro.add(idFiltro);
				} else if (idReporte.trim().equals("2")){
					//PROMOCIÓN
					idFiltro = new CampoRptDTO();
					idFiltro.setClave("PROMOCION");
					idFiltro.setValor(idFuncion.trim());
					lstFiltro.add(idFiltro);
					
				}
			}
			
			if(idFuncion != null && !idFuncion.trim().equals("") && !idFuncion.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("ID_FUNCION");
				idFiltro.setValor(idFuncion.trim());
				lstFiltro.add(idFiltro);
			}

			if(idAsignatura != null && !idAsignatura.trim().equals("") && !idAsignatura.trim().equals("0")) {
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("CVEASIGNATURA");
				idFiltro.setValor(idAsignatura.trim());
				lstFiltro.add(idFiltro);
			}

			if (fecInicial != null) {
				SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
				
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("FECRECHAZO_INICIAL");
				idFiltro.setValor(formatoFecha.format(fecInicial));
				idFiltro.setCadena(true);
				
				lstFiltro.add(idFiltro);
			}

			if (fecFinal != null) {
				SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
				
				idFiltro = new CampoRptDTO();
				idFiltro.setClave("FECRECHAZO_FINAL");
				idFiltro.setValor(formatoFecha.format(fecFinal));
				idFiltro.setCadena(true);
				
				lstFiltro.add(idFiltro);
			}
			
			AspiranteMSDAO aspiranteDAO = new AspiranteMSDaoImpl();
			lstAspirante = aspiranteDAO.getDetalleAspirantes(lstFiltro);
			//lstCifra = aspiranteDAO.getCifrasRechazo(lstFiltro);
			
			if(lstAspirante.isEmpty()) {
				FacesMessage msg = new FacesMessage("No se localizarón registros con los filtros seleccionados.");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				mostrarDetalle();
			}
				
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "consultarDetalle() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return resultado;
	}
	
	public void limpiar () {
		idConvocatoria = "0";
		idSubsistema = "0";
		idReporte = "0";
		idFuncion = "0";
		idAsignatura = "0";
		bSubsistema = false;
		fecInicial = null;
		fecFinal = null;
		lstAspirante = new ArrayList<AspiranteDTO>();
		lstCifra = new ArrayList<CifraDTO>();

	}

    public void enCambioDeReporte () {
    	
    	try {
        	CatalogoDAO catalogoDAO = new CatalogoDaoImpl();
        	
            if(idReporte !=null && idReporte.equals("1")) {
            	//Ingreso
            	bSubsistema = false;
    			List<CampoRptDTO> lstFiltroFun = new ArrayList<CampoRptDTO>();
    			List<CampoRptDTO> lstFiltroAsg = new ArrayList<CampoRptDTO>();
    			CampoRptDTO idFiltroFun;
    			CampoRptDTO idFiltroAsg;
            	
				idFiltroFun = new CampoRptDTO();
				idFiltroFun.setClave("NOM_CAT");
				idFiltroFun.setValor("ASGMSFUNASP");
				lstFiltroFun.add(idFiltroFun);

				idFiltroFun = new CampoRptDTO();
				idFiltroFun.setClave("ID_IGUAL_A");
				idFiltroFun.setValor("1");
				lstFiltroFun.add(idFiltroFun);

				lstFuncion = catalogoDAO.getCatalogo(lstFiltroFun);

				idFiltroAsg = new CampoRptDTO();
				idFiltroAsg.setClave("NOM_CAT");
				idFiltroAsg.setValor("ASGMSASIGN");
				lstFiltroAsg.add(idFiltroAsg);

				idFiltroAsg = new CampoRptDTO();
				idFiltroAsg.setClave("ID_MENOR_IGUAL_QUE");
				idFiltroAsg.setValor("100");
				lstFiltroAsg.add(idFiltroAsg);

				lstAsignatura = catalogoDAO.getCatalogo(lstFiltroAsg);
				
            } else if(idReporte !=null && idReporte.equals("2")) {
            	//Promoción
            	bSubsistema = true;
    			List<CampoRptDTO> lstFiltroFun = new ArrayList<CampoRptDTO>();
    			List<CampoRptDTO> lstFiltroAsg = new ArrayList<CampoRptDTO>();
    			CampoRptDTO idFiltroFun;
    			CampoRptDTO idFiltroAsg;
            	
				idFiltroFun = new CampoRptDTO();
				idFiltroFun.setClave("NOM_CAT");
				idFiltroFun.setValor("ASGMSFUNASP");
				lstFiltroFun.add(idFiltroFun);

				idFiltroFun = new CampoRptDTO();
				idFiltroFun.setClave("ID_DIFERENTE_DE");
				idFiltroFun.setValor("1");
				lstFiltroFun.add(idFiltroFun);

				lstFuncion = catalogoDAO.getCatalogo(lstFiltroFun);

				idFiltroAsg = new CampoRptDTO();
				idFiltroAsg.setClave("NOM_CAT");
				idFiltroAsg.setValor("ASGMSASIGN");
				lstFiltroAsg.add(idFiltroAsg);

				idFiltroAsg = new CampoRptDTO();
				idFiltroAsg.setClave("ID_MAYOR_IGUAL_QUE");
				idFiltroAsg.setValor("101");
				lstFiltroAsg.add(idFiltroAsg);

				lstAsignatura = catalogoDAO.getCatalogo(lstFiltroAsg);
            	
            } else {
            	bSubsistema = false;
    			lstFuncion = catalogoDAO.getCatalogoItems("ASGMSFUNASP");
    			lstAsignatura = catalogoDAO.getCatalogoItems("ASGMSASIGN");             	
            }

		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "enCambioDeReporte() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
    	            

    }

    public String inicializarDetalle() {
    	String resultado = null;
    	
    	mostrarDetalle();
    	
    	return resultado;		
    }
    
    public String regresarConsulta() {
    	String resultado = null;
    	
    	mostrarConsulta();
    	
    	return resultado;		
    }

    public void postProcesoXLS(Object document) {
        HSSFWorkbook wb = (HSSFWorkbook) document;
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow header = sheet.getRow(0);
         
        HSSFCellStyle cellStyle = wb.createCellStyle();
        
        HSSFFont hSSFFont = wb.createFont();
        hSSFFont.setFontName(HSSFFont.FONT_ARIAL);
        hSSFFont.setFontHeightInPoints((short)10);
        hSSFFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        hSSFFont.setColor(HSSFColor.WHITE.index);
        cellStyle.setFont(hSSFFont);
        cellStyle.setFillForegroundColor(HSSFColor.DARK_RED.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
         
        for(int i=0; i < header.getPhysicalNumberOfCells();i++) {
            HSSFCell cell = header.getCell(i);
             
            cell.setCellStyle(cellStyle);
        }
        
        sheet.setDefaultColumnWidth(15);
    }

	private void mostrarConsulta() {
		bndConsulta = true;
		bndDetalle = false;
	}

	private void mostrarDetalle() {
		bndConsulta = false;
		bndDetalle = true;
	}

	public String getIdConvocatoria() {
		return idConvocatoria;
	}

	public void setIdConvocatoria(String idConvocatoria) {
		this.idConvocatoria = idConvocatoria;
	}

	public String getIdRechazo() {
		return idRechazo;
	}

	public void setIdRechazo(String idRechazo) {
		this.idRechazo = idRechazo;
	}

	public String getIdReporte() {
		return idReporte;
	}

	public void setIdReporte(String idReporte) {
		this.idReporte = idReporte;
	}

	public String getIdSubsistema() {
		return idSubsistema;
	}

	public void setIdSubsistema(String idSubsistema) {
		this.idSubsistema = idSubsistema;
	}

	public String getIdFuncion() {
		return idFuncion;
	}

	public void setIdFuncion(String idFuncion) {
		this.idFuncion = idFuncion;
	}

	public String getIdAsignatura() {
		return idAsignatura;
	}

	public void setIdAsignatura(String idAsignatura) {
		this.idAsignatura = idAsignatura;
	}

	public Date getFecInicial() {
		return fecInicial;
	}

	public void setFecInicial(Date fecInicial) {
		this.fecInicial = fecInicial;
	}

	public Date getFecFinal() {
		return fecFinal;
	}

	public void setFecFinal(Date fecFinal) {
		this.fecFinal = fecFinal;
	}

	public boolean isbSubsistema() {
		return bSubsistema;
	}

	public void setbSubsistema(boolean bSubsistema) {
		this.bSubsistema = bSubsistema;
	}

	public List<CatalogoDTO> getLstConvocatoria() {
		return lstConvocatoria;
	}

	public void setLstConvocatoria(List<CatalogoDTO> lstConvocatoria) {
		this.lstConvocatoria = lstConvocatoria;
	}

	public List<CatalogoDTO> getLstRechazo() {
		return lstRechazo;
	}

	public void setLstRechazo(List<CatalogoDTO> lstRechazo) {
		this.lstRechazo = lstRechazo;
	}

	public List<CatalogoDTO> getLstFuncion() {
		return lstFuncion;
	}

	public void setLstFuncion(List<CatalogoDTO> lstFuncion) {
		this.lstFuncion = lstFuncion;
	}

	public List<CatalogoDTO> getLstSubsistema() {
		return lstSubsistema;
	}

	public void setLstSubsistema(List<CatalogoDTO> lstSubsistema) {
		this.lstSubsistema = lstSubsistema;
	}

	public List<CatalogoDTO> getLstAsignatura() {
		return lstAsignatura;
	}

	public void setLstAsignatura(List<CatalogoDTO> lstAsignatura) {
		this.lstAsignatura = lstAsignatura;
	}

	public List<AspiranteDTO> getLstAspirante() {
		return lstAspirante;
	}

	public void setLstAspirante(List<AspiranteDTO> lstAspirante) {
		this.lstAspirante = lstAspirante;
	}

	public List<CifraDTO> getLstCifra() {
		return lstCifra;
	}

	public void setLstCifra(List<CifraDTO> lstCifra) {
		this.lstCifra = lstCifra;
	}

	public boolean isBndConsulta() {
		return bndConsulta;
	}

	public void setBndConsulta(boolean bndConsulta) {
		this.bndConsulta = bndConsulta;
	}

	public boolean isBndDetalle() {
		return bndDetalle;
	}

	public void setBndDetalle(boolean bndDetalle) {
		this.bndDetalle = bndDetalle;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}
	
}
