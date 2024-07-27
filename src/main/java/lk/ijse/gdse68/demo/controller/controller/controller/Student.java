package lk.ijse.gdse68.demo.controller.controller.controller;

import jakarta.json.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.gdse68.demo.controller.controller.dto.StudentDTO;
import lk.ijse.gdse68.demo.controller.controller.util.Util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/student")
public class Student extends HttpServlet {
    Connection connection;
    public static String SAVE_STUDENT ="INSERT INTO student (id,name,email,city,level)VALUES(?,?,?,?,?)";
    public static String GET_STUDENT = "SELECT * FROM student WHERE id=?";
    public static  String UPDATE_STUDENT = "UPDATE student SET name=?,email=?,city=?,level=? WHERE id=?";
    public static String DELETE_STUDENT = "DELETE FROM student WHERE id=?";


    @Override
    public void init() throws ServletException {

        try {
            /*var dbClass = getServletContext().getInitParameter("db-class");
            var dbUrl = getServletContext().getInitParameter("db-url");
            var dbUser = getServletContext().getInitParameter("db-user");
            var dbPassword = getServletContext().getInitParameter("db-password");
            Class.forName(dbClass);*/

          var ctx =  new InitialContext(); //pool eken contex ekk ganna
          DataSource pool = (DataSource) ctx.lookup("java:comp/env/jdbc/student"); //Arroer casting
           this.connection = pool.getConnection();



        } catch (NamingException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //*  var intParam = getServletContext().getInitParameter("Hello Param");
        //System.out.println(intParam);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(req.getContentType() ==null || !req.getContentType().toLowerCase().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        }

        ///object active of json

            try(var Writer = resp.getWriter()){
                Jsonb jsonB = JsonbBuilder.create();
                StudentDTO student = jsonB.fromJson(req.getReader(), StudentDTO.class);
                student.setId(Util.idGenerate());

                try {
                    var ps = connection.prepareStatement(SAVE_STUDENT);
                    ps.setString(1,student.getId());
                    ps.setString(2,student.getName());
                    ps.setString(3,student.getEmail());
                    ps.setString(4,student.getCity());
                    ps.setString(5,student.getLevel());

                    if (ps.executeUpdate()>0){
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        Writer.write("Save Student Success");
                    }
                    else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        Writer.write("Save Student Failed");
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    throw new RuntimeException(e);
                }
            }



        //create respons

       // resp.setContentType("application/json");
        //jsonB.toJson(student, resp.getWriter());
    }

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try(var write  = response.getWriter()){
            StudentDTO studentDTO = new StudentDTO();
           Jsonb jsonb = JsonbBuilder.create();
           var ID =request.getParameter("studentId");
           var ps = connection.prepareStatement(GET_STUDENT);
            ps.setString(1,ID);
            var rest = ps.executeQuery();

            while (rest.next()){
                studentDTO.setId(rest.getString("id"));
                studentDTO.setName(rest.getString("name"));
                studentDTO.setEmail(rest.getString("email"));
                studentDTO.setCity(rest.getString("city"));
                studentDTO.setLevel(rest.getString("level"));
            }

            response.setContentType("application/json");
            jsonb.toJson(studentDTO, write);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//update

        try (var writer = resp.getWriter()){
            var id = req.getParameter("studentId");
                Jsonb jsonb = JsonbBuilder.create();
               StudentDTO studentDTO =  jsonb .fromJson(req.getReader(), StudentDTO.class);


               var ps = connection.prepareStatement(UPDATE_STUDENT);
               ps.setString(1,studentDTO.getName());
               ps.setString(2,studentDTO.getEmail());
               ps.setString(3,studentDTO.getCity());
               ps.setString(4,studentDTO.getLevel());
               ps.setString(5,id);

               if (ps.executeUpdate()>0){
                   resp.setStatus(HttpServletResponse.SC_CREATED);
                   writer.write("UPdate Student Success");
               }else {
                   resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                   writer.write("Update Student Failed");
               }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException(e);
        }


        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (var writer = resp.getWriter()){
            var id =req.getParameter("studentId");
                Jsonb jsonb = JsonbBuilder.create();
                StudentDTO studentDTO =  jsonb .fromJson(req.getReader(), StudentDTO.class);

               var ps = connection.prepareStatement(DELETE_STUDENT);
               ps.setString(1,id);
               if (ps.executeUpdate()>0){
                   resp.setStatus(HttpServletResponse.SC_CREATED);
                   writer.write("Delete Student Success");
               }else {
                   resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                   writer.write("Delete Student Failed");
               }

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException(e);
        }

        }


    }
