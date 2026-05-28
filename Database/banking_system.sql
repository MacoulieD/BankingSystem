-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 28-05-2026 a las 02:34:01
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `banking_system`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cuentas`
--

CREATE TABLE `cuentas` (
  `id` int(11) NOT NULL,
  `numero_cuenta` varchar(50) NOT NULL,
  `propietario` varchar(50) NOT NULL,
  `tipo_cuenta` varchar(20) NOT NULL,
  `saldo` decimal(15,2) DEFAULT 0.00,
  `estado` varchar(20) DEFAULT 'ACTIVA',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cuenta_ahorros`
--

CREATE TABLE `cuenta_ahorros` (
  `id` int(11) NOT NULL,
  `numero_cuenta` varchar(50) NOT NULL,
  `propietario` varchar(50) NOT NULL,
  `saldo` decimal(15,2) DEFAULT 0.00,
  `tasa_interes` decimal(5,3) DEFAULT 0.500,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cuenta_corriente`
--

CREATE TABLE `cuenta_corriente` (
  `id` int(11) NOT NULL,
  `numero_cuenta` varchar(50) NOT NULL,
  `propietario` varchar(50) NOT NULL,
  `saldo` decimal(15,2) DEFAULT 0.00,
  `sobregiro_permitido` decimal(15,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimientos`
--

CREATE TABLE `movimientos` (
  `id` int(11) NOT NULL,
  `numero_cuenta` varchar(50) NOT NULL,
  `tipo_movimiento` varchar(20) NOT NULL,
  `monto` decimal(15,2) NOT NULL,
  `saldo_anterior` decimal(15,2) DEFAULT NULL,
  `saldo_posterior` decimal(15,2) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `fecha` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `person`
--

CREATE TABLE `person` (
  `id_person` int(11) NOT NULL,
  `name` varchar(60) NOT NULL,
  `telephone` varchar(10) NOT NULL,
  `email` varchar(50) NOT NULL,
  `userName` varchar(10) NOT NULL,
  `userpassword` varchar(10) NOT NULL,
  `initialBalance` decimal(10,0) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tarjetas_credito`
--

CREATE TABLE `tarjetas_credito` (
  `id` int(11) NOT NULL,
  `numero_tarjeta` varchar(20) NOT NULL,
  `propietario` varchar(50) NOT NULL,
  `cvv` varchar(3) NOT NULL,
  `limite_credito` decimal(15,2) DEFAULT 0.00,
  `saldo_actual` decimal(15,2) DEFAULT 0.00,
  `fecha_vencimiento` varchar(5) DEFAULT NULL,
  `activa` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `cuentas`
--
ALTER TABLE `cuentas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_cuenta` (`numero_cuenta`),
  ADD KEY `propietario` (`propietario`);

--
-- Indices de la tabla `cuenta_ahorros`
--
ALTER TABLE `cuenta_ahorros`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_cuenta` (`numero_cuenta`),
  ADD KEY `propietario` (`propietario`);

--
-- Indices de la tabla `cuenta_corriente`
--
ALTER TABLE `cuenta_corriente`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_cuenta` (`numero_cuenta`),
  ADD KEY `propietario` (`propietario`);

--
-- Indices de la tabla `movimientos`
--
ALTER TABLE `movimientos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `numero_cuenta` (`numero_cuenta`);

--
-- Indices de la tabla `person`
--
ALTER TABLE `person`
  ADD PRIMARY KEY (`id_person`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `userName` (`userName`);

--
-- Indices de la tabla `tarjetas_credito`
--
ALTER TABLE `tarjetas_credito`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_tarjeta` (`numero_tarjeta`),
  ADD KEY `propietario` (`propietario`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `cuentas`
--
ALTER TABLE `cuentas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `cuenta_ahorros`
--
ALTER TABLE `cuenta_ahorros`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `cuenta_corriente`
--
ALTER TABLE `cuenta_corriente`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `movimientos`
--
ALTER TABLE `movimientos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `tarjetas_credito`
--
ALTER TABLE `tarjetas_credito`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `cuentas`
--
ALTER TABLE `cuentas`
  ADD CONSTRAINT `cuentas_ibfk_1` FOREIGN KEY (`propietario`) REFERENCES `person` (`userName`);

--
-- Filtros para la tabla `cuenta_ahorros`
--
ALTER TABLE `cuenta_ahorros`
  ADD CONSTRAINT `cuenta_ahorros_ibfk_1` FOREIGN KEY (`propietario`) REFERENCES `person` (`userName`),
  ADD CONSTRAINT `cuenta_ahorros_ibfk_2` FOREIGN KEY (`numero_cuenta`) REFERENCES `cuentas` (`numero_cuenta`);

--
-- Filtros para la tabla `cuenta_corriente`
--
ALTER TABLE `cuenta_corriente`
  ADD CONSTRAINT `cuenta_corriente_ibfk_1` FOREIGN KEY (`propietario`) REFERENCES `person` (`userName`),
  ADD CONSTRAINT `cuenta_corriente_ibfk_2` FOREIGN KEY (`numero_cuenta`) REFERENCES `cuentas` (`numero_cuenta`);

--
-- Filtros para la tabla `movimientos`
--
ALTER TABLE `movimientos`
  ADD CONSTRAINT `movimientos_ibfk_1` FOREIGN KEY (`numero_cuenta`) REFERENCES `cuentas` (`numero_cuenta`);

--
-- Filtros para la tabla `tarjetas_credito`
--
ALTER TABLE `tarjetas_credito`
  ADD CONSTRAINT `tarjetas_credito_ibfk_1` FOREIGN KEY (`propietario`) REFERENCES `person` (`userName`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
