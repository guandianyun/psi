<p></p>
<p></p>

![Descripción de la imagen](https://gitee.com/o88o/psi/raw/master/readme-pic/logo.png) 

<div align="center">

[Sitio web oficial](https://www.guanxdian.com/) | [Demo en línea](https://guanxdian.cn/demo) 

</div>

<p></p>
<p></p>
<p></p>
<p></p>

---

**<p align="center">【📣Aviso reciente】Si tiene alguna sugerencia amistosa o opinión sobre el producto/la tecnología, contáctenos, se incluirán como contribuyentes de código abierto y se agradecerán en el sitio web oficial, ¡y también recibirán pequeños regalos del premio de la comunidad!~</p>**

**<p align="center">¡La nube de la tienda de gestión V2.0 se ha lanzado oficialmente, y el código fuente se publicará gradualmente, ¡esperamos que lo experimente temprano! <a href="https://www.guanxdian.com" target="_blank">Haga clic aquí para experimentarlo ahora~</a></p>**

**<p align="center">Si cree que nuestro proyecto de código abierto le ha sido de utilidad, haga clic en :star: Star para apoyar al equipo de código abierto de la nube de la tienda de gestión:heart:</p>**

---

# I. Acerca de la nube de la tienda de gestión

## 1.1 Introducción

> La nube de la tienda de gestión, la tecnología conecta la industria.

La nube de la tienda de gestión ofrece soluciones de gestión de inventarios para la industria física tradicional, implementando escenarios empresariales completos como compras, inventarios, ventas, caja y contabilidad, ayudando a la industria tradicional a lograr una transformación digital, mejorar la eficiencia de la gestión, aumentar su capacidad de operar de manera digital y hacer que la gestión de personas, cuentas y bienes sea más fácil.

Se utiliza principalmente en el sector minorista, comercio por mayor y fábricas de producción.
Las industrias de todos los campos pueden personalizar sus propias necesidades en función de la nube de la tienda de gestión.

## 1.2 Capacidad

La versión de la comunidad de la nube de la tienda de gestión v2.0 será de código abierto gradualmente:
El sistema completo se divide en once módulos principales:

*   **Gestión de ventas**: Incluye funciones como órdenes de venta, gestión de devoluciones, gestión de clientes, gestión de niveles de precios, etc.;
*   **Gestión de compras**: Incluye funciones como órdenes de compra, órdenes de devolución de compra, estadísticas de compras, etc.;
*   **Gestión de inventarios**: Incluye funciones como consultas de inventarios, inventario de conteo, flujo de inventarios, etc.;
*   **Gestión financiera y de fondos**: Incluye funciones como la conciliación de cobros de clientes, la conciliación de pagos a proveedores, ingresos y egresos diarios, transferencias entre cuentas de liquidación, flujo de fondos, estadísticas de ganancias operativas, etc.;
*   **Gestión de productos**: Incluye funciones como la clasificación de productos, gestión de especificaciones, gestión de unidades, gestión de atributos, gestión de productos, etc.;
*   **Configuración del sistema**: Incluye funciones como la gestión de empleados, gestión de roles, configuración del sistema, configuración de impresión, registros de operaciones, etc.;

Damos la bienvenida a los contribuyentes interesados para que participen activamente en la nueva versión de la nube de la tienda de gestión de código abierto. Si están interesados, contáctenos a través de la información de contacto a continuación.

## 1.3 Ventajas

La nube de la tienda de gestión ofrece los flujos de negocios principales de inventarios y ventas, con las funciones más centrales de inventarios y ventas, y las principales ventajas son:

* Utiliza la arquitectura de Java principal, con alta escalabilidad y flexibilidad, evitando las desventajas de la arquitectura PHP;
* El código es conciso, con un desarrollo secundario de bajo costo, lo que le permite tener más tiempo para pasar con su esposa, hijos o novia;
* El producto tiene una buena experiencia de usuario y se puede aprender y usar rápidamente.

## 1.4 Visión

La nube de la tienda de gestión no es solo para tecnólogos, sino que utiliza la tecnología para que cada empresa tradicional tenga productos de software más útiles, soluciones prácticas, un buen software no es porque tenga muchas funciones, sino porque realmente puede ayudar a las empresas tradicionales a reducir costos y aumentar la eficiencia.

# II. Acerca del proyecto

## 2.1 Experiencia en línea

Dirección de la demostración de código abierto: [https://www.guanxdian.com](https://www.guanxdian.com)

Cuenta predeterminada y contraseña: Obtener en WeChat `guanxdian`

_(Tenga en cuenta: El entorno de la demostración ha bloqueado los permisos de administración y operaciones relacionadas)_

## 2.2 Pila de tecnología

* Pila de tecnología del lado del cliente: Bootstrap, JQuery, Echarts, Layer;
* Pila de tecnología del lado del servidor: JFinal, Redis, Mysql;

## 2.3 Estructura del proyecto

**Estructura del proyecto**
```
├── psi-common             // Módulo de componentes comunes
├── psi-fund             // Módulo de fondos financieros
├── psi-purchase         // Módulo de compra e inventario
├── psi-sale             // Módulo relacionado con las ventas
├── psi-web             // Módulo de negocio de PC
```

## 2.4 Implementación del proyecto

**Entorno del servidor:**
* Mysql: >= 5.7
* Versión jdk: >= jdk1.8 (se recomienda la versión 1.8)
* Redis: >= redis6.2
* Sistema operativo: linux, windows

**Configuración mínima recomendada del servidor:**

| Tipo | Configuración |
|:-:|:-:|
|Sistema Operativo|CentOS 7 o superior|
|CPU|2 núcleos|
|Memoria|4G|
|Ancho de banda|1M|
|Disco duro|100G|

## 2.5 Capturas de pantalla del producto

**Página de inicio:**
![Página de inicio](https://gitee.com/o88o/psi/raw/master/readme-pic/demo-home.png "Página de inicio")

**Mostrador de caja:**
![Mostrador de caja](https://gitee.com/o88o/psi/raw/master/readme-pic/demo-shouyin.png "Mostrador de caja")

**Plantilla de impresión:**
![Plantilla de impresión](https://gitee.com/o88o/psi/raw/master/readme-pic/demo-print.png "Plantilla de impresión")

**Configuración del sistema:**
![Configuración del sistema](https://gitee.com/o88o/psi/raw/master/readme-pic/demo-setting.png "Configuración del sistema")

**Nueva órden de venta:**
![Nueva órden de venta](https://gitee.com/o88o/psi/raw/master/readme-pic/demo-salecadd.png "Nueva órden de venta")

**Lista de órdenes de venta:**
![Lista de órdenes de venta](https://gitee.com/o88o/psi/raw/master/readme-pic/demo-saleorder.png "Lista de órdenes de venta")

# III. Preguntas frecuentes

En desarrollo...


# IV. Últimas actualizaciones

En desarrollo...


# V. Contáctenos

Si desea comunicarse con nosotros sobre el código abierto, tiene alguna idea, opinión o sugerencia sobre el producto de la nube de la tienda de gestión, o tiene necesidades de colaboración comercial, por favor escanee el código para agregar al equipo del proyecto de la nube de la tienda de gestión para la comunicación:

![Contáctenos](https://gitee.com/o88o/psi/raw/master/readme-pic/contact.png)

# VI. Apoyo a través de donaciones

## 7.1 Un pot de té de bayabas goji

Si cree que nuestro proyecto de código abierto GuanDianYun le ha sido de ayuda, ¡por favor, trate a los desarrolladores del proyecto a un pot de té de bayabas goji! Actualmente, aceptamos donaciones de WeChat, Alipay o Gitee. Por favor, anote su apodo o un mensaje adjunto al donar.

Su donación se utilizará para cubrir algunos gastos del proyecto y para motivar a los desarrolladores para impulsar mejor el desarrollo del proyecto, también se acoge donaciones de servidor público en la red para mejorar la experiencia del sistema de demostración en línea.

![](https://gitee.com/o88o/psi/raw/master/readme-pic/paychannel.png "")

## 7.2 Donaciones a largo plazo

Si usted es un operador de negocios y tiene planes para utilizar GuanDianYun en los productos comerciales de la empresa, se le da la bienvenida para hacer donaciones a largo plazo. Las donaciones a largo plazo tienen ventajas comerciales, como:

* Respuesta activa, mantenimiento rápido y actualizaciones oportunas;
* El nombre de la empresa, el logotipo y el enlace del sitio web se mostrarán a largo plazo en el repositorio de código abierto, en el sitio web de GuanDianYun y en los materiales de promoción;
* La cantidad donada se deducirá proporcionalmente del precio de los productos de pago futuros de `GuanDianYun` .

Si está interesado en patrocinar a largo plazo al equipo de GuanDianYun, o tiene otras buenas ideas, se le invita a contactar al equipo de desarrollo a través de WeChat guanxdian, o envíe un correo electrónico a 275477265@qq.com.

---

**<p align="center">Si cree que nuestro proyecto de código abierto le ha sido de ayuda, por favor haga clic en :star: Star para apoyar al equipo de código abierto de GuanDianYun:heart:</p>**

---

