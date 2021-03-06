package mx.gob.edomex.seduca.dgippe.sigdip.asignacion.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.dao.AspiranteDAO;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.dto.AspiranteDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.impl.AspiranteDaoImpl;
import mx.gob.edomex.seduca.dgippe.sigdip.bean.login.LoginBean;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.dao.CatalogoDAO;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.dto.CatalogoDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.impl.CatalogoDaoImpl;
import mx.gob.edomex.seduca.dgippe.sigdip.comun.dto.CampoRptDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.util.db.Conexion;
import mx.gob.edomex.seduca.dgippe.sigdip.util.validador.Validador;

@ManagedBean
@ViewScoped
public class RechazoFolioBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean bndConsulta;
	private boolean bndRechazo;
	private String folio;
	private List<AspiranteDTO> lstAspirante;
	private AspiranteDTO aspirante;
	private AspiranteDTO aspiranteFormato;
	private List<CatalogoDTO> lstMotivoRechazo;
	private String idMotivoRechazo;
	private String observacionRechazo;
	private LoginBean loginBean;
	
	public RechazoFolioBean() {
		mostrarConsulta();
		
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
			loginBean = (LoginBean) session.getAttribute("loginBean");	
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "RechazoFolioBean() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public String consultarFolio() {
		String resultado = null;
		
		lstAspirante = new ArrayList<AspiranteDTO>();
		
		try {
			if(folio != null && !folio.trim().equals("")) {
				Validador IValidador = new Validador();
				if(IValidador.isNumeros(folio) && folio.trim().length() == 12) {
					List<CampoRptDTO> lstFiltro = new ArrayList<CampoRptDTO>();					
					CampoRptDTO idFiltro;
										
					idFiltro = new CampoRptDTO();
					idFiltro.setClave("FOLIO");
					idFiltro.setValor(folio.trim());
					lstFiltro.add(idFiltro);

					String usuCveAsignatura = loginBean.getUsuario().getIdAsignatura();
					if (usuCveAsignatura != null && !usuCveAsignatura.trim().equals("") && !usuCveAsignatura.trim().equals("0")) {
						idFiltro = new CampoRptDTO();
						idFiltro.setClave("CVEASIGNATURA");
						idFiltro.setValor(usuCveAsignatura.trim());
						lstFiltro.add(idFiltro);						
					}

					AspiranteDAO aspiranteDAO = new AspiranteDaoImpl();
					lstAspirante = aspiranteDAO.getAspirantes(lstFiltro);
					
					if(lstAspirante.isEmpty()) {
						FacesMessage msg = new FacesMessage("No se localizarón registros con los filtros seleccionados.");
						msg.setSeverity(FacesMessage.SEVERITY_ERROR);
						FacesContext.getCurrentInstance().addMessage(null, msg);
					} else {
						aspirante = lstAspirante.get(0);
					}
				} else {
					FacesMessage msg = new FacesMessage("Folio debe ser un dato numérico de longitud 12.");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, msg);									
				}
			} else {
				FacesMessage msg = new FacesMessage("Folio es un campo requerido, por favor verifica.");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, msg);				
			}
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "consultarFolio() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return resultado;
	}

	public String inicializarRechazo() {
		String resultado = null;
		
		idMotivoRechazo = "0";
		observacionRechazo = "";
		
		try {
			CatalogoDAO catalogoDAO = new CatalogoDaoImpl();
			lstMotivoRechazo = catalogoDAO.getCatalogoItems("ASGMTVRECH");
			
			mostrarRechazo();
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "inicializarRechazo() - Se generó un error interno en el servidor.") );
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return resultado;
	}

	public String registrarRechazo() {
		String resultado = null;
		
		try {
			if (aspirante != null && aspirante.getFolioFederal() != null && !aspirante.getFolioFederal().trim().equals("")) {
				if (idMotivoRechazo != null && !idMotivoRechazo.trim().equals("") && !idMotivoRechazo.trim().equals("0")) {
					if (observacionRechazo != null && !observacionRechazo.trim().equals("")) {
						String folioFederal = aspirante.getFolioFederal();
						String idConvocatoria = aspirante.getIdConvocatoria();
						String idFuncion = aspirante.getIdFuncion();
						String usuario = loginBean.getUsuario().getCveUsuario();
						
						AspiranteDAO aspiranteDAO = new AspiranteDaoImpl();
						String respuesta = aspiranteDAO.registrarRenuncia(folioFederal, idConvocatoria, idFuncion, idMotivoRechazo, observacionRechazo.trim().toUpperCase(), usuario);
						
						if (respuesta.startsWith("00")) {
							this.folio = aspirante.getFolioFederal();
													
							consultarFolio();
							
							mostrarConsulta();
							
							respuesta = respuesta.substring(3); //Eliminar "00|"
							FacesMessage msg = new FacesMessage(respuesta);
							msg.setSeverity(FacesMessage.SEVERITY_INFO);
							FacesContext.getCurrentInstance().addMessage(null, msg);
						} else {
							FacesMessage msg = new FacesMessage(respuesta);
							msg.setSeverity(FacesMessage.SEVERITY_ERROR);
							FacesContext.getCurrentInstance().addMessage(null, msg);
						}						
					} else {
						FacesMessage msg = new FacesMessage("Observación es requerido, por favor selecciona uno.");
						msg.setSeverity(FacesMessage.SEVERITY_ERROR);
						FacesContext.getCurrentInstance().addMessage(null, msg);						
					}
					
				} else {
					FacesMessage msg = new FacesMessage("Motivo de rechazo es requerido, por favor selecciona uno.");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			} else {
				FacesMessage msg = new FacesMessage("asignarPlaza() : Ocurrió un error al recuperar la información del aspirante.");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "registrarRenuncia() - Se generó un error interno en el servidor.") );
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return resultado;
	}

	public String generarFormato() {
		String resultado = null;
		
		try {
			if(aspiranteFormato != null  && aspiranteFormato.getFolioFederal() != null && !aspiranteFormato.getFolioFederal().trim().equals("")
					&& aspiranteFormato.getIdConvocatoria() != null && !aspirante.getIdConvocatoria().trim().equals("") 
					&& aspiranteFormato.getCveAsignatura() != null && !aspirante.getCveAsignatura().trim().equals("") && !aspirante.getCveAsignatura().trim().equals("0")
					&& aspiranteFormato.getStatus() != null && !aspiranteFormato.getStatus().trim().equals("") && !aspiranteFormato.getStatus().trim().equals("0")) {
				
				String cveStatus = aspiranteFormato.getStatus().trim();
				
				if (cveStatus.equals("2") || cveStatus.equals("3")) {
					String folioFederal = aspiranteFormato.getFolioFederal();
					String idConvocatoria = aspiranteFormato.getIdConvocatoria();
					String idFuncion = aspiranteFormato.getIdFuncion();
					//String tipoConvocatoria = null;
					//String idSubsistema = null;
					//boolean existeNombramiento = false;
					//ArchivoDTO archivoNombramiento = null;
					
					/*
					if(aspiranteFormato.getTipoConvocatoria().trim().toUpperCase().indexOf("NORMAL")>=0) {
						tipoConvocatoria = "N";
					} if(aspiranteFormato.getTipoConvocatoria().trim().toUpperCase().indexOf("ABIERTA")>=0) {
						tipoConvocatoria = "A";
					}
					*/
										
					String fileName = folioFederal + ".pdf";
					
					String imgEdomex = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/img/gemcenter.jpg");
					String imgGrande = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/img/engrande.jpg");

					String nombreJASPER = null;
					Map<String, Object> params = new HashMap<String, Object>();
					
					if(cveStatus.equals("3")) {
						String idMotivoRechazo = "0";
						AspiranteDAO aspiranteDAO = new AspiranteDaoImpl();
						idMotivoRechazo = aspiranteDAO.getIdMotivoRechazoAspirante(folioFederal, idConvocatoria, idFuncion); 

						List<String> lstCNA = Arrays.asList("2", "3", "5");
						List<String> lstACNP = Arrays.asList("4", "6", "10");
						List<String> lstORAFR = Arrays.asList("9", "1", "7", "8", "11", "12");
						List<String> lstACNFNAN = Arrays.asList("14");
						List<String> lstORP = Arrays.asList("21");
						
						if (idMotivoRechazo != null && !idMotivoRechazo.trim().equals("0") && !idMotivoRechazo.trim().equals("")) {
							if (lstCNA.contains(idMotivoRechazo)) {
								//CARTA DE NO ACEPTACION.
								fileName = "NO_ACEPTACION_" + fileName;
								nombreJASPER = "/jasper/carta_no_acept_ba.jasper";
								String idReporte = idFuncion.equals("1") ? "CNA_BA_ING" : "CNA_BA_PRO";

								params.put("P_FOLIOFEDER", folioFederal);
								params.put("P_ID_CONVOCATORIA", idConvocatoria);
								params.put("P_ID_FUNCION", idFuncion);
								params.put("P_ID_RPT", idReporte);
								params.put("P_IMG_GEM", imgEdomex);
								params.put("P_IMG_EG", imgGrande);						

							} else if (lstACNP.contains(idMotivoRechazo)) {
								//ACTA NO PRESENTACION
								fileName = "NO_PRESENTACION_" + fileName;
								nombreJASPER = "/jasper/acta_no_presenta_ba.jasper";
								String idReporte = idFuncion.equals("1") ? "ANP_BA_ING" : "ANP_BA_PRO";

								params.put("P_FOLIOFEDER", folioFederal);
								params.put("P_ID_CONVOCATORIA", idConvocatoria);
								params.put("P_ID_FUNCION", idFuncion);
								params.put("P_ID_RPT", idReporte);
								params.put("P_IMG_GEM", imgEdomex);
								params.put("P_IMG_EG", imgGrande);
								
							} else if (lstORAFR.contains(idMotivoRechazo)) {
								//OFICIO DE RECHAZO ASPIRANTE
								fileName = "OFICIO_RECHAZO_" + fileName;
								nombreJASPER = "/jasper/oficio_rechazo_ba.jasper";
								String idReporte = idFuncion.equals("1") ? "OR_BA_ING" : "OR_BA_PRO";

								params.put("P_FOLIOFEDER", folioFederal);
								params.put("P_ID_CONVOCATORIA", idConvocatoria);
								params.put("P_ID_FUNCION", idFuncion);
								params.put("P_ID_RPT", idReporte);
								params.put("P_IMG_GEM", imgEdomex);
								params.put("P_IMG_EG", imgGrande);
								
							} else if (lstACNFNAN.contains(idMotivoRechazo)) {
								//NEGATIVA FIRMA
								fileName = "NEGATIVA_FIRMA_" + fileName;
								nombreJASPER = "/jasper/negativa_acept_nombr_ba.jasper";
								String idReporte = idFuncion.equals("1") ? "NF_BA_ING" : "NF_BA_PRO";

								params.put("P_FOLIOFEDER", folioFederal);
								params.put("P_ID_CONVOCATORIA", idConvocatoria);
								params.put("P_ID_FUNCION", idFuncion);
								params.put("P_ID_RPT", idReporte);
								params.put("P_IMG_GEM", imgEdomex);
								params.put("P_IMG_EG", imgGrande);
								
							} else if (lstORP.contains(idMotivoRechazo)) { //Agregado por ATD
								//OFICIO DE RENUNCIA A LA PRELACIÓN
								fileName = "OFICIO_RENUNCIA_" + fileName;
								//nombreJASPER = "/jasper/negativa_acept_nombr_ba.jasper";
								nombreJASPER = idFuncion.equals("1") ? "/jasper/oficio_renuncia_ba_ing.jasper":"/jasper/oficio_renuncia_ba_pro.jasper";
								String idReporte = idFuncion.equals("1") ? "ORP_BA_ING" : "ORP_BA_PRO";

								params.put("P_FOLIOFEDER", folioFederal);
								params.put("P_ID_CONVOCATORIA", idConvocatoria);
								params.put("P_ID_FUNCION", idFuncion);
								params.put("P_ID_RPT", idReporte);
								params.put("P_IMG_GEM", imgEdomex);
								params.put("P_IMG_EG", imgGrande);	
								params.put("P_NOM_CAT", "ASGFUNASP");
								params.put("P_NOM_CAT_SUB", "ASGSUBSIS");

							} else {
								FacesMessage msg = new FacesMessage("No existe un formato para el motivo de rechazo " +idMotivoRechazo + ".");
								msg.setSeverity(FacesMessage.SEVERITY_ERROR);
								FacesContext.getCurrentInstance().addMessage(null, msg);
								return null;															
							}
						} else {
							FacesMessage msg = new FacesMessage("No se localizó el motivo de rechazo o el tipo de convocatoria.");
							msg.setSeverity(FacesMessage.SEVERITY_ERROR);
							FacesContext.getCurrentInstance().addMessage(null, msg);
							return null;							
						}
					} 
					
					byte[] bytes = null;

					Conexion cnn = new Conexion();
					cnn.conectar();
					
					String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(nombreJASPER);
					JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, cnn.getConexion());
					
					bytes = JasperExportManager.exportReportToPdf(jasperPrint);
					
					cnn.desconectar();
					
					FacesContext context = FacesContext.getCurrentInstance();
					HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
					
					response.setContentType("application/pdf");
					response.setHeader("Content-Disposition", "attachment;filename=\""+fileName+"\";");
					response.getOutputStream().write(bytes);
					response.setStatus(HttpServletResponse.SC_OK);
					response.getOutputStream().close();					
					
					context.responseComplete();					
				} else {
					FacesMessage msg = new FacesMessage("Para generar un formato, el aspirante debe tener estatus ASIGNADO o RECHAZADO.");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				
			} else {
				FacesMessage msg = new FacesMessage("No se pudo recuperar la información del aspirante o el tipo formato requerido, inténta nuevamente.");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, msg);				
			}
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "generarFormato() - Se generó un error interno en el servidor.") );
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		return resultado;
	}

	public void regresarConsulta() {
		folio = null;
		mostrarConsulta();
	}

	public void limpiar() {
		folio = null;
		lstAspirante = new ArrayList<AspiranteDTO>();
	}

	private void mostrarConsulta() {
		bndConsulta = true;
		bndRechazo = false;
	}

	private void mostrarRechazo() {
		bndConsulta = false;
		bndRechazo = true;
	}

	public boolean isBndConsulta() {
		return bndConsulta;
	}

	public void setBndConsulta(boolean bndConsulta) {
		this.bndConsulta = bndConsulta;
	}

	public boolean isBndRechazo() {
		return bndRechazo;
	}

	public void setBndRechazo(boolean bndRechazo) {
		this.bndRechazo = bndRechazo;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public List<AspiranteDTO> getLstAspirante() {
		return lstAspirante;
	}

	public void setLstAspirante(List<AspiranteDTO> lstAspirante) {
		this.lstAspirante = lstAspirante;
	}

	
	public AspiranteDTO getAspiranteFormato() {
		return aspiranteFormato;
	}

	public void setAspiranteFormato(AspiranteDTO aspiranteFormato) {
		this.aspiranteFormato = aspiranteFormato;
	}

	public AspiranteDTO getAspirante() {
		return aspirante;
	}

	public void setAspirante(AspiranteDTO aspirante) {
		this.aspirante = aspirante;
	}

	public List<CatalogoDTO> getLstMotivoRechazo() {
		return lstMotivoRechazo;
	}

	public void setLstMotivoRechazo(List<CatalogoDTO> lstMotivoRechazo) {
		this.lstMotivoRechazo = lstMotivoRechazo;
	}

	public String getIdMotivoRechazo() {
		return idMotivoRechazo;
	}

	public void setIdMotivoRechazo(String idMotivoRechazo) {
		this.idMotivoRechazo = idMotivoRechazo;
	}

	public String getObservacionRechazo() {
		return observacionRechazo;
	}

	public void setObservacionRechazo(String observacionRechazo) {
		this.observacionRechazo = observacionRechazo;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

}
