/*
 * Autor - Alberto Rodriguez Porral - 15-12-2022.   
 * CargarCarreras = este programa realiza diversas acciones a fin de poder fusionar las carreras almacenadas en la antigua base de datos 'piramboia.carreras' con los datos
 * obtenidos en el fichero 'activities.json' descargado de la app móvil NRC. Este programa con el método BorrarCarrerasnrc() inicializa la tabla piramboia.carrerasnrc utilizando 
 * un TRUNCATE a fin de poner el 'id' a 0. A continuación en el método CargarDesdeTabla() carga en la tabla 'piramboia.carrerasnrc' los datos de la tabla 'piramboia.carreras'. 
 * El siguiente paso es cargar las carreras descargadas de la app NRC en el fichero 'activities.json' con el método CargarDesdeFichero(Matriz matriz).
 * Por último realizamos el borrado de los registros duplicados en el método BorrarDuplicados().
 *
 */
package com.titus.cargarcarreras;

import com.google.gson.Gson;
import com.titus.carreras.Matriz;
import com.titus.carreras.Summaries;
import com.titus.tablas.Carrerasnrc;
import com.titus.tablas.Carreras;
import com.titus.tablas.Clavesnrc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TemporalType;

public class CargarCarreras {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("carrerasPU");
    static int num1 = 0;
    static int num2 = 0;
    static int duplicados = 0;

    public static void main(String[] args) throws IOException, SQLException {

        Gson gson = new Gson();
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        String fichero = null;

        BorrarCarrerasnrc();

        CargarDesdeTabla();

        String ficheros = "F:\\ANALISIS Y PROGRAMACION\\Programas java\\CarrerasNRC\\ficheros\\activities.json";

        try ( Stream<String> stream = Files.lines(Paths.get(ficheros), Charset.defaultCharset())) {

//      !! utilizando expresión lambda !!
//            stream.map(l -> gson.fromJson(l, Matriz.class))                    
//                    .collect(toList()).forEach(m -> CargarDesdeFichero(m));
            
//      !! utilizando dobles dos puntos !!
            stream.map(l -> gson.fromJson(l, Matriz.class))                    
                    .collect(toList()).forEach(CargarCarreras::CargarDesdeFichero);
            
        } catch (IOException e) {
        }        
        
//      !! Por último, borramos los duplicados, ya que durante el 2016 y 2017 he anotado las carreras en la tabla antigua y en la app de NRC !!
        BorrarDuplicados();
        
        System.out.println("Se han insertado "+(num1+num2-duplicados)+" carreras en la tabla de Carrerasnrc.");
    }

    public static void BorrarCarrerasnrc() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/piramboia?useTimezone=true&serverTimezone=UTC&useSSL=false";
        String userName = "root";
        String password = "demiurgo";
        
        // hacemos un "truncate" en vez de un "delete" porque de este modo se inicializa el "id"
        String truncateTableQuery = "truncate table Carrerasnrc";
        
        try (Connection conn = DriverManager.getConnection(url, userName, password)) {
            Statement statement = conn.createStatement();
            statement.executeUpdate(truncateTableQuery);            
            System.out.println("Tabla Carrerasnrc truncada!...");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Ha ocurrido una excepcion en el truncamiento de la tabla Carrerasnrc!");
        } 
    }

    public static void CargarDesdeTabla() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        List<Carreras> li = (List<Carreras>) em.createQuery("select o from Carreras as o order by o.fecha").getResultList();

        tx.begin();

        li.stream().forEach(carreras -> {
            // Duracion
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date duracion = new Date(carreras.getHoraFin().getTime() - carreras.getHoraInicio().getTime());
            
            Carrerasnrc c = new Carrerasnrc();
            c.setFecha(carreras.getFecha());
            c.setDuracion(duracion);
            c.setKms(carreras.getKms());
            c.setRecorrido(carreras.getRecorrido());
            c.setTipoDeEjercicio("correr");
            c.setSensaciones("");
            c.setClima("");
            c.setCalzado("");
            c.setCalorias(0.0);
            c.setPasos(0);
            c.setPeso(carreras.getPeso());
            c.setTemperatura(0.0);
            c.setTerreno("");

            em.persist(c);

            num1++;

            tx.commit();
            tx.begin();
            em.clear();
        });
        
        tx.commit();
        em.close();
    }

    public static void CargarDesdeFichero(Matriz matriz) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        List<Clavesnrc> li = (List<Clavesnrc>) em.createQuery("select o from Clavesnrc as o").getResultList();

        Map<String, String> sensa = li.stream()
                .filter(m -> "sensacion".equals(m.getTipo()))
                .collect(Collectors.toMap(m -> m.getClave(), m -> m.getDescripcion()));
        Map<String, String> tiempo = li.stream()
                .filter(m -> "clima".equals(m.getTipo()))
                .collect(Collectors.toMap(m -> m.getClave(), m -> m.getDescripcion()));
        Map<String, String> firme = li.stream()
                .filter(m -> "terreno".equals(m.getTipo()))
                .collect(Collectors.toMap(m -> m.getClave(), m -> m.getDescripcion()));
        Map<String, String> tenis = li.stream()
                .filter(m -> "calzado".equals(m.getTipo()))
                .collect(Collectors.toMap(m -> m.getClave(), m -> m.getDescripcion()));

        tx.begin();

        matriz.getActivities().stream().forEach(nrc -> {
            // Fecha
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date fecha = new Date(nrc.getStart_epoch_ms().longValue());

            // Duracion
            sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date duracion = new Date(nrc.getActive_duration_ms().longValue());

            // Kms, calorias y pasos (se encuentran en el array de summaries).
            double kms = 0.0;
            double calorias = 0.0;
            double peso = 0.0;
            double temperatura = 0.0;
            int pasos = 0;

            for (Summaries summary : nrc.getSummaries()) {
                if (summary.getMetric().equals("distance")) {
                    kms = summary.getValue();
                }
                if (summary.getMetric().equals("calories")) {
                    calorias = summary.getValue();
                }
                if (summary.getMetric().equals("steps")) {
                    pasos = (int) summary.getValue();
                }
            }
            
            // recorrido, sensaciones, calzado, clima, terreno y temperatura (se encuentran en el array de tags).
            String nombre = nrc.getTags().getComNikeName();
            String sensaciones = nrc.getTags().getEmotion();
            String calzado = (nrc.getTags().getShoes() == null) ? nrc.getTags().getShoe_id() : nrc.getTags().getShoes();
            String terreno = nrc.getTags().getTerrain();
            String clima = (nrc.getTags().getWeather() == null) ? nrc.getTags().getComNikeWeather() : nrc.getTags().getWeather();
            String temp = (nrc.getTags().getTemperature() == null) ? nrc.getTags().getComNikeTemperature() : nrc.getTags().getTemperature().substring(0, 2);
            
            temperatura = (temp == null || temp.equals("TE"))? 0.0 : Double.parseDouble(temp);
            nombre = (nombre == null)? "" : nombre;
            sensaciones = (sensaciones == null)? "" : sensaciones;
            terreno = (terreno == null)? "" : terreno;
            calzado = (calzado == null)? "" : calzado;
            clima = (clima == null)? "" : clima;
            
            // si las calorias vienen a cero calculamos las que debería haber consumido utilizando la media de calorias por km.
            // he calculado que para todas mis carreras esa media es de 91,4 calorias/km
            calorias = (calorias == 0)? (calorias = kms * 91.4) : calorias;
            
            for (String key : sensa.keySet()) {
                if (sensaciones != null && key.equals(sensaciones.toUpperCase())) {
                    sensaciones = sensa.get(key);
                }
            }
            for (String key : tiempo.keySet()) {
                if (clima != null && key.equals(clima.toUpperCase())) {
                    clima = tiempo.get(key);
                }
            }
            for (String key : firme.keySet()) {
                if (terreno != null && key.equals(terreno.toUpperCase())) {
                    terreno = firme.get(key);
                }
            }
            for (String key : tenis.keySet()) {
                if (calzado != null && key.equals(calzado)) {
                    calzado = tenis.get(key);
                }
            }

            Carrerasnrc c = new Carrerasnrc();
            c.setFecha(fecha);
            c.setDuracion(duracion);
            c.setKms(kms);
            c.setRecorrido(nombre);
            c.setTipoDeEjercicio("correr");
            c.setSensaciones(sensaciones);
            c.setClima(clima);
            c.setCalzado(calzado);
            c.setCalorias(calorias);
            c.setPasos(pasos);
            c.setPeso(peso);
            c.setTemperatura(temperatura);
            c.setTerreno(terreno);

            em.persist(c);

            num2++;

            tx.commit();
            tx.begin();
            em.clear();
        });
        tx.commit();
        em.close();
    }
    
    public static void BorrarDuplicados() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        
        List<String> fechas = Arrays.asList("2016-03-22","2016-03-24","2016-03-26","2016-03-28","2016-03-30","2016-03-31","2016-04-02","2016-04-04","2016-04-06","2016-04-08","2016-04-10",
                "2016-04-13","2016-04-16","2016-04-18","2016-04-20","2016-04-22","2016-04-24","2016-04-26","2016-04-28","2016-05-02","2016-05-04","2016-05-08","2016-05-09",
                "2016-05-12","2016-05-14","2016-05-16","2016-05-18","2016-05-20","2016-05-22","2016-06-02","2016-06-05","2016-06-08","2016-06-11","2016-06-13","2016-06-17",
                "2016-06-19","2016-06-24","2016-06-27","2016-06-30","2016-07-04","2016-07-06","2016-07-13","2016-07-18","2016-07-20","2016-07-30","2016-08-01","2016-08-03",
                "2016-08-07","2016-08-09","2016-08-10","2016-08-16","2016-08-19","2016-08-22","2016-08-25","2016-08-27","2016-08-29","2016-08-31","2016-09-05","2016-09-07",
                "2016-09-10","2016-09-12","2016-09-13","2016-09-15","2016-09-18","2016-09-21","2016-09-24","2016-10-04","2016-10-09","2016-10-12","2016-10-16","2016-10-19",
                "2016-10-24","2016-10-26","2016-10-29","2016-10-31","2016-11-03","2016-11-07","2016-11-28","2016-12-03","2016-12-11","2016-12-13","2016-12-26","2016-12-28",
                "2016-12-31","2017-01-08","2017-01-09","2017-01-12","2017-05-17");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));        
        
        fechas.stream().forEach(f -> {
            Date utilDate = null;        
            try {
                utilDate = sdf.parse(f);
            } catch (ParseException ex) {
                Logger.getLogger(CargarCarreras.class.getName()).log(Level.SEVERE, null, ex);
            }
            duplicados += em.createQuery("delete from Carrerasnrc where fecha=:fecha and calorias=:calorias").setParameter("fecha", utilDate, TemporalType.DATE).
                    setParameter("calorias", 0.0).executeUpdate();
        });
        
        System.out.println("Se han borrado los "+duplicados+" registros duplicados de la tabla Carrerasnrc");
        
        tx.commit();
        em.close();  
    }
}