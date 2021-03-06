package mx.gob.edomex.seduca.dgippe.sigdip.asignacionms.bean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.primefaces.event.SelectEvent;

import java.util.Date;

import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.impl.CitaDaoImpl;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.dao.CitaDAO;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.dto.AspiranteDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.asignacion.dto.CitaDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.bean.login.LoginBean;
import mx.gob.edomex.seduca.dgippe.sigdip.util.db.Conexion;
import mx.gob.edomex.seduca.dgippe.sigdip.util.excepcion.DBExcepcion;
import mx.gob.edomex.seduca.dgippe.sigdip.util.excepcion.SistemaExcepcion;
import mx.gob.edomex.seduca.dgippe.sigdip.util.validador.Validador;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.dto.CatalogoDTO;
import mx.gob.edomex.seduca.dgippe.sigdip.catalogo.impl.CatalogoDaoImpl;;

@ManagedBean
@ViewScoped
public class CitaBeanMS implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean banPanelConsulta=true;
	private boolean banPanelCita;
	private String folio;
	private Date fecha;
	private Date hora;
	private AspiranteDTO asp ;
	private List<AspiranteDTO>citas;
	private List<AspiranteDTO>folios;
	private List<CatalogoDTO> subsistemas;
	private String curp;
	private String subsistema;
	private String ciclo;
	
	private LoginBean loginBean;
	
	public CitaBeanMS()
	{
		try {

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
			loginBean = (LoginBean) session.getAttribute("loginBean");	
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "CitaBeanMS() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}		
	}	
	
	/**
	 * Metodo para cambiar dinamicamente de sujeto cuando se trata de folio repetido
	 * @param event
	 */
	public void valueChanged(ValueChangeEvent event) 
	{
	    for(AspiranteDTO f: folios)
	    {
	    	if(f.getCurp().equals(curp))
	    	{
	    		asp = f;
	    	}
	    }
	}
	/**
	 * Metodo para insertar una cita
	 */
	public void insertaCita()
	{
		int re =0;
		CitaDAO cita =  new CitaDaoImpl();
		CitaDTO c = new CitaDTO();
		String reportDate = concatFecha(fecha,hora);
		try
		{
			c = cita.consultaCita(folio, curp); //consulta si el folio ya esta dado de alta en la base de datos
			if(c.getFolio()==null) // Si no existe se inserta
				re = cita.insertaCita(folio, reportDate,curp);
			else if(c.getFolio()!=null)// Si existe se actualiza y lanza una advertencia
			{
				re = cita.actualizaCita(folio, reportDate,curp);
				FacesMessage message = new FacesMessage("Se actualizará la cita");
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				FacesContext.getCurrentInstance().addMessage(null,message);
			}
			if(re!=0)
			{
				FacesMessage message = new FacesMessage("Cita registrada");
				message.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null,message);
			}
			else
			{
				FacesMessage message = new FacesMessage("Error al registrar cita");
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null,message);
			}
		}
		catch (Exception e) 
		{
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "insertaCita() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}	
	}
	
	public void consultaFolioMS()
	{
		CitaDAO cita = new CitaDaoImpl();
		try
		{
			if(new Validador().isNumeros(folio))
			{
				folios= cita.consultaFolioMS(folio);
				if(folios.size()<=0)
				{
					FacesMessage message = new FacesMessage("Folio no válido");
					message.setSeverity(FacesMessage.SEVERITY_ERROR);
					FacesContext.getCurrentInstance().addMessage(null,message);
				}
				else
				{
					asp = folios.get(0);
					if(folios.size()>1)
					{
						FacesMessage message = new FacesMessage("El folio se encuentra repetido: Asegúrese de elegir a la persona correcta");
						message.setSeverity(FacesMessage.SEVERITY_WARN);
						FacesContext.getCurrentInstance().addMessage(null,message);
					}
					banPanelCita = true;
					banPanelConsulta = false;
				}
			}
			else
			{
				FacesMessage message = new FacesMessage("Folio debe ser un valor numérico");
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null,message);				
			}			
		} 
		catch (Exception e) 
		{
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "consultaFolioMS() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}		
	}
	
	public void consultaCitasMS()
	{
		CitaDAO cita = new CitaDaoImpl();
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String reportDate = df.format(fecha);
		try 
		{
			citas=cita.consultaCitasMS(reportDate,subsistema);
			if(citas.size()<=0 )
			{
				FacesMessage message = new FacesMessage("No se encontraron citas con los criterios de búsqueda");
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				FacesContext.getCurrentInstance().addMessage(null,message);
			}
			else
			{
				FacesMessage message = new FacesMessage("Búsqueda Exitosa");
				message.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null,message);
				banPanelCita=true;
				banPanelConsulta=false;
			}
			
		} 
		catch (Exception e) 
		{
			FacesMessage msg = new FacesMessage((e.getMessage()!=null ? e.getMessage() : "consultaCitasMS() : Se generó un error interno en el servidor."));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}		
		
	}

	public void generaListaMS() throws DBExcepcion
	{
		Conexion cnn = null;
		String nombreJASPER = null;
		Map<String, Object> params = new HashMap<String, Object>();
		if(citas.size()> 0)
		{
			nombreJASPER = "/jasper/ListaAsistenciaMS.jasper";
			byte[] bytes = null;
			banPanelConsulta=false;
			
			String imgEdomex = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/img/gemcenter.jpg");
			String imgGrande = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/img/engrande.jpg");
			
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String reportDate = df.format(fecha);
			String fileName = "Lista_De_Asistencia-MS-" +subsistema+"-"+ reportDate + ".pdf";
			params.put("fecha", reportDate);
			params.put("subsistema", subsistema);
			params.put("ciclo", ciclo);
			params.put("P_IMG_GEM", imgEdomex);
			params.put("P_IMG_EG", imgGrande);

			try
			{
				cnn = new Conexion();
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}finally {
	            if(cnn != null){
	                cnn.desconectar();
	            }
	        }
		}
		else
		{
			FacesMessage message = new FacesMessage("No hay registros con los criterios de búsqueda");
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			FacesContext.getCurrentInstance().addMessage(null,message);
		}
	}
	
	public void consultaSubsistemas()
	{
		
		try 
		{
			subsistemas = new CatalogoDaoImpl().getCatalogoSubItems("ASGMSSUBSIS","497");
		} 
		catch (DBExcepcion de) 
		{
			de.printStackTrace();
		}
		catch (SistemaExcepcion e) 
		{
			e.printStackTrace();
		}		
	}
/**
 * Inicializa fecha hora y una instancia de aspirante
 */
	@PostConstruct
	public void init()
	{
		consultaSubsistemas();
		ciclo = "2015-2016";
		asp = new AspiranteDTO();
		fecha = new Date();
		hora = new Date();
	}
	
	public String concatFecha(Date fecha, Date hora)
	{
		DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        return formatoFecha.format(fecha)+" "+formatoHora.format(hora);
	}

	public void onDateSelect(SelectEvent event) 
	{
        
    }	

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public boolean isBanPanelConsulta() 
	{
		return banPanelConsulta;
	}

	public void setBanPanelConsulta(boolean banPanelConsulta) 
	{
		this.banPanelConsulta = banPanelConsulta;
	}

	public boolean isBanPanelCita() 
	{
		return banPanelCita;
	}

	public void setBanPanelCita(boolean banPanelCita) 
	{
		this.banPanelCita = banPanelCita;
	}

	public AspiranteDTO getAsp() {
		return asp;
	}

	public void setAsp(AspiranteDTO asp) {
		this.asp = asp;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getHora() {
		return hora;
	}

	public void setHora(Date hora) {
		this.hora = hora;
	}
	public List<AspiranteDTO> getCitas() {
		return citas;
	}
	public void setCitas(List<AspiranteDTO> citas) {
		this.citas = citas;
	}
	public List<AspiranteDTO> getFolios() {
		return folios;
	}
	public void setFolios(List<AspiranteDTO> folios) {
		this.folios = folios;
	}

	public String getCurp() {
		return curp;
	}

	public void setCurp(String curp) {
		this.curp = curp;
	}
	public List<CatalogoDTO> getSubsistemas() {
		return subsistemas;
	}
	public void setSubsistemas(List<CatalogoDTO> subsistemas) {
		this.subsistemas = subsistemas;
	}

	public String getSubsistema() {
		return subsistema;
	}
	public void setSubsistema(String subsistema) {
		this.subsistema = subsistema;
	}
	public String getCiclo() {
		return ciclo;
	}
	public void setCiclo(String ciclo) {
		this.ciclo = ciclo;
	}
	
}
