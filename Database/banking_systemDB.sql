-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 30-05-2026 a las 06:30:26
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.1.25

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

--
-- Volcado de datos para la tabla `cuentas`
--

INSERT INTO `cuentas` (`id`, `numero_cuenta`, `propietario`, `tipo_cuenta`, `saldo`, `estado`, `created_at`) VALUES
(1, 'CA-001', 'agd', 'AHORROS', 1000000.00, 'ACTIVA', '2026-05-29 06:07:17'),
(4, 'CA-002', '13', 'AHORROS', 160000000.00, 'ACTIVA', '2026-05-29 07:03:56'),
(5, 'CA-003', 'andy', 'AHORROS', 99950000.00, 'ACTIVA', '2026-05-30 01:26:48'),
(9, 'CC-004', 'andy', 'CORRIENTE', 180000000.00, 'ACTIVA', '2026-05-30 01:50:48'),
(14, 'CA-005', 'pepe', 'AHORROS', 10000000.00, 'ACTIVA', '2026-05-30 04:20:23');

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

--
-- Volcado de datos para la tabla `cuenta_ahorros`
--

INSERT INTO `cuenta_ahorros` (`id`, `numero_cuenta`, `propietario`, `saldo`, `tasa_interes`, `created_at`) VALUES
(1, 'CA-001', 'agd', 1000000.00, 0.015, '2026-05-29 06:07:17'),
(2, 'CA-002', '13', 160000000.00, 0.015, '2026-05-29 07:03:56'),
(3, 'CA-003', 'andy', 99950000.00, 0.015, '2026-05-30 01:26:48'),
(9, 'CA-005', 'pepe', 10000000.00, 0.015, '2026-05-30 04:20:23');

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

--
-- Volcado de datos para la tabla `cuenta_corriente`
--

INSERT INTO `cuenta_corriente` (`id`, `numero_cuenta`, `propietario`, `saldo`, `sobregiro_permitido`, `created_at`) VALUES
(1, 'CC-004', 'andy', 180000000.00, 6000000.00, '2026-05-30 01:50:48');

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

--
-- Volcado de datos para la tabla `movimientos`
--

INSERT INTO `movimientos` (`id`, `numero_cuenta`, `tipo_movimiento`, `monto`, `saldo_anterior`, `saldo_posterior`, `descripcion`, `fecha`) VALUES
(1, 'CA-003', 'CONSIGNACION', 1000000.00, 10000000.00, 11000000.00, 'Consignación: +$1.000.000,00', '2026-05-30 01:28:11'),
(2, 'CA-003', 'CONSIGNACION', 200000000.00, 11000000.00, 211000000.00, 'Consignación: +$200.000.000,00', '2026-05-30 01:43:01'),
(3, 'CA-003', 'RETIRO', 11000000.00, 211000000.00, 200000000.00, 'Retiro: -$11.000.000,00', '2026-05-30 01:45:51'),
(4, 'CC-004', 'CONSIGNACION', 50000000.00, 30000000.00, 80000000.00, 'Consignación: +$50.000.000,00', '2026-05-30 01:51:18'),
(5, 'CA-003', 'TRANSFERENCIA_OUT', 100000000.00, 200000000.00, 100000000.00, 'Transferencia propia enviada a CC-004 (andy): -$100.000.000,00', '2026-05-30 01:53:51'),
(6, 'CC-004', 'TRANSFERENCIA_IN', 100000000.00, 80000000.00, 180000000.00, 'Transferencia propia recibida desde CA-003 (andy): +$100.000.000,00', '2026-05-30 01:53:51');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `person`
--

CREATE TABLE `person` (
  `id_person` int(11) NOT NULL,
  `name` varchar(60) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `userName` varchar(10) NOT NULL,
  `userpassword` varchar(10) NOT NULL,
  `is_blocked` datetime DEFAULT NULL,
  `failed_attempts` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `person`
--

INSERT INTO `person` (`id_person`, `name`, `telephone`, `email`, `userName`, `userpassword`, `is_blocked`, `failed_attempts`) VALUES
(1, 'a', '1111111', 'q@g.com', 'a', '1212', NULL, 0),
(2, 's', '1111111', 'q@q.com', 's', '1212', NULL, 0),
(3, 'w', '2222222', 'q@e.com', 'q', '1212', NULL, 0),
(13, 'and', '22222222', 'qs@qs.com', 'and', '1212', NULL, 0),
(21, 'agd', '123412232', 'q@agd.com', 'agd', '3232', NULL, 0),
(22, 'pepe', '34153134', 'pp@q.com', 'pepe', '22', NULL, 0),
(23, 'ed', '55555555', 'ed@ed.com', 'ed', '2323', NULL, 0),
(34, 'lic', '5555555', 'lic@g.com', 'lic', '3434', NULL, 0),
(1001, 'Andres', '300886638', 'and@q.com', 'andy', '32', NULL, 0),
(1212, 'oto', '8876543', 'oto@oto.com', '13', '13', NULL, 0),
(123456, 'admin', 'admin@gil.com', 'admin', '12345678', 'admin', NULL, 0),
(11037844, 'Daniel Guerra', '4334343', 'Da', 'Daniel', '2006', NULL, 0),
(1066176423, 'daniel guerra', '3242129377', 'dan@gmail.com', 'daniel01', '2006', NULL, 0);

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
  `deuda_actual` decimal(15,2) DEFAULT 0.00,
  `fecha_vencimiento` varchar(5) DEFAULT NULL,
  `activa` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tarjetas_credito`
--

INSERT INTO `tarjetas_credito` (`id`, `numero_tarjeta`, `propietario`, `cvv`, `limite_credito`, `deuda_actual`, `fecha_vencimiento`, `activa`, `created_at`) VALUES
(1, 'TC-005', 'andy', '123', 4000000.00, 0.00, '12-31', 1, '2026-05-30 02:08:03');

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `cuenta_ahorros`
--
ALTER TABLE `cuenta_ahorros`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `cuenta_corriente`
--
ALTER TABLE `cuenta_corriente`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `movimientos`
--
ALTER TABLE `movimientos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `tarjetas_credito`
--
ALTER TABLE `tarjetas_credito`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

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
