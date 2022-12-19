/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.titus.tablas;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author arpor
 */
@Entity
@Table(name = "clavesnrc")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Clavesnrc.findAll", query = "SELECT c FROM Clavesnrc c"),
    @NamedQuery(name = "Clavesnrc.findById", query = "SELECT c FROM Clavesnrc c WHERE c.id = :id"),
    @NamedQuery(name = "Clavesnrc.findByTipo", query = "SELECT c FROM Clavesnrc c WHERE c.tipo = :tipo"),
    @NamedQuery(name = "Clavesnrc.findByClave", query = "SELECT c FROM Clavesnrc c WHERE c.clave = :clave"),
    @NamedQuery(name = "Clavesnrc.findByDescripcion", query = "SELECT c FROM Clavesnrc c WHERE c.descripcion = :descripcion")})
public class Clavesnrc implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "tipo")
    private String tipo;
    @Column(name = "clave")
    private String clave;
    @Column(name = "descripcion")
    private String descripcion;

    public Clavesnrc() {
    }

    public Clavesnrc(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Clavesnrc)) {
            return false;
        }
        Clavesnrc other = (Clavesnrc) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "com.titus.tablas.Clavesnrc[ id=" + id + " ]";
    }
    
}
