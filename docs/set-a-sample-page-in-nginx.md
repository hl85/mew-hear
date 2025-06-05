
在 Debian 服务器的 Nginx 中部署一个默认网页，显示“网站建设中”，可以按照以下步骤操作：

1\. 确保 Nginx 已安装

首先，确认 Nginx 已安装并运行。如果未安装，可以使用以下命令安装：

bash

```
sudo apt update
sudo apt install nginx
```

启动 Nginx 并确保它开机自启：

bash

```
sudo systemctl start nginx
sudo systemctl enable nginx
```

检查 Nginx 是否运行：

bash

```
sudo systemctl status nginx
```

2\. 创建默认网页

Nginx 默认网站目录通常位于 /var/www/html。你可以在此目录下创建一个简单的 HTML 文件。

创建或编辑 index.html文件：

bash

```
sudo nano /var/www/html/index.html
```

在文件中添加以下内容：

html

```
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>网站建设中</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background-color: #f0f0f0;
        }
        h1 {
            color: #333;
        }
    </style>
</head>
<body>
    <h1>网站建设中，敬请期待！</h1>
</body>
</html>
```

保存并退出（Ctrl+O，Enter，Ctrl+X）。

3\. 设置文件权限

确保 Nginx 可以访问该文件：

bash

```
sudo chown -R www-data:www-data /var/www/html
sudo chmod -R 755 /var/www/html
```

4\. 配置 Nginx

默认情况下，Nginx 已经配置为使用 /var/www/html作为网站根目录。如果需要修改或确认，检查默认配置文件：

打开 Nginx 默认配置文件：

bash

```
sudo nano /etc/nginx/sites-available/default
```

确保 root指令指向/var/www/html，并且 index包含index.html：

nginx

```
server {
    listen 80;
    server_name _;
    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

保存并退出。

5\. 测试和重启 Nginx

测试 Nginx 配置是否正确：

bash

```
sudo nginx -t
```

如果配置无误，重启 Nginx：

bash

```
sudo systemctl restart nginx
```

6\. 访问网页

在浏览器中输入服务器的 IP 地址（例如 http://your\_server\_ip），应该能看到“网站建设中，敬请期待！”的页面。

7\. （可选）防火墙设置

如果服务器启用了防火墙，确保 80 端口开放：

bash

```
sudo ufw allow 80
sudo ufw status
```

注意事项

如果有域名，需在 Nginx 配置文件中设置 server\_name为你的域名，并确保 DNS 已指向服务器 IP。

如果需要 HTTPS，考虑使用 Let’s Encrypt 安装 SSL 证书（可用 certbot工具）。

如果网页无法访问，检查 Nginx 日志：

bash

```
sudo tail -f /var/log/nginx/error.log
sudo tail -f /var/log/nginx/access.log
```
